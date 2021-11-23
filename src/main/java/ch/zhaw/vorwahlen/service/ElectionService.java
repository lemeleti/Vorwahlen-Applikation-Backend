package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.exporter.ModuleElectionExporter;
import ch.zhaw.vorwahlen.model.dto.ElectionTransferDTO;
import ch.zhaw.vorwahlen.model.dto.ModuleElectionDTO;
import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import ch.zhaw.vorwahlen.model.modules.Student;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleDefinition;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleStructureGenerator;
import ch.zhaw.vorwahlen.modulevalidation.ElectionValidator;
import ch.zhaw.vorwahlen.repository.ElectionRepository;
import ch.zhaw.vorwahlen.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.util.function.Function;

/**
 * Business logic for the election.
 */
@Service
@Log
@RequiredArgsConstructor
public class ElectionService {
    private final ElectionRepository electionRepository;
    private final ModuleRepository moduleRepository;
    private final Function<Student, ElectionValidator> validatorFunction;
    private final Function<Student, ModuleDefinition> structureFunction;
    private final ModuleElectionExporter exporter;

    /**
     * Get election data for the specified user.
     * @param student get election for student
     * @return election data
     */
    public ElectionTransferDTO getElection(Student student) {
        var moduleElection = loadModuleElectionForStudent(student);
        return createElectionTransferDTO(student, moduleElection, false);
    }

    /**
     * Get election for the specified user
     * @param student get election for student
     * @return module election from db if existent otherwise a new instance will be created.
     */
    public ModuleElectionDTO getModuleElectionForStudent(Student student) {
        return DTOMapper.mapElectionToDto.apply(loadModuleElectionForStudent(student));
    }

    /**
     * Saves the election to the database.
     * @param student student in session
     * @param moduleNo module that should be saved
     * @return ElectionTransferDTO containing the election data
     */
    public ElectionTransferDTO saveElection(Student student, String moduleNo) {
        var moduleElection = loadModuleElectionForStudent(student);
        var electionValidator = validatorFunction.apply(student);
        migrateElectionChanges(moduleElection, moduleNo);
        var isValid = electionValidator.validate(moduleElection);

        moduleElection.setElectionValid(isValid);
        electionRepository.save(moduleElection);
        return createElectionTransferDTO(student, moduleElection, true);
    }

    /**
     * Export all module elections.
     * @return byte array containing the formatted module election.
     */
    public byte[] exportModuleElection() {
        return exporter.export(electionRepository.findAll());
    }

    private void migrateElectionChanges(ModuleElection moduleElection, String moduleNo) {
        var module = moduleRepository.findById(moduleNo).orElseThrow();

        // todo check if user is allowed to elect module
        var electedModules = moduleElection.getElectedModules();
        if (!electedModules.removeIf(m -> moduleNo.equals(m.getModuleNo()))) {
            electedModules.add(module);
        }
    }

    private ElectionTransferDTO createElectionTransferDTO(Student student,
                                                          ModuleElection moduleElection, boolean saved) {
        var moduleDefinition = structureFunction.apply(student);
        var electionStructure =
                new ModuleStructureGenerator(moduleDefinition, student, moduleElection).generateStructure();
        return new ElectionTransferDTO(electionStructure, saved, moduleElection.isElectionValid());
    }

    private ModuleElection loadModuleElectionForStudent(Student student) {
        return electionRepository.findModuleElectionByStudent(student.getEmail()).orElse(new ModuleElection());
    }
}
