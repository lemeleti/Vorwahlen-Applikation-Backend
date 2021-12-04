package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.exporter.ModuleElectionExporter;
import ch.zhaw.vorwahlen.mapper.Mapper;
import ch.zhaw.vorwahlen.model.dto.ElectionStatusDTO;
import ch.zhaw.vorwahlen.model.dto.ElectionTransferDTO;
import ch.zhaw.vorwahlen.model.dto.ModuleElectionDTO;
import ch.zhaw.vorwahlen.model.modules.*;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleDefinition;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleStructureGenerator;
import ch.zhaw.vorwahlen.validation.ElectionValidator;
import ch.zhaw.vorwahlen.repository.ElectionRepository;
import ch.zhaw.vorwahlen.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;

/**
 * Business logic for the election.
 */
@Service
@Log
@RequiredArgsConstructor
public class ElectionService {
    private final ElectionRepository electionRepository;
    private final ModuleRepository moduleRepository;
    private final ElectionValidator electionValidator;
    private final ModuleDefinition moduleDefinition;
    private final ModuleElectionExporter exporter;
    private final ElectionSemesters electionSemesters;
    private final Mapper<ModuleElectionDTO, ModuleElection> moduleElectionMapper;
    private final Mapper<ElectionStatusDTO, ModuleElectionStatus> electionStatusMapper;

    /**
     * Get election data for the specified user.
     * @param student get election for student
     * @return election data
     */
    public ElectionTransferDTO getElection(Student student) {
        var moduleElection = loadModuleElectionForStudent(student);
        var moduleElectionStatus = electionValidator.validate(moduleElection);
        return createElectionTransferDTO(student, moduleElectionStatus, moduleElection, false);
    }

    /**
     * Get election for the specified user
     * @param student get election for student
     * @return module election from db if existent otherwise a new instance will be created.
     */
    public ModuleElectionDTO getModuleElectionForStudent(Student student) {
        return moduleElectionMapper.toDto(loadModuleElectionForStudent(student));
    }

    /**
     * Saves the election to the database.
     * @param student student in session
     * @param moduleNo module that should be saved
     * @return ElectionTransferDTO containing the election data
     */
    public ElectionTransferDTO saveElection(Student student, String moduleNo) {
        var moduleElection = loadModuleElectionForStudent(student);
        migrateElectionChanges(moduleElection, moduleNo);
        var moduleElectionStatus = electionValidator.validate(moduleElection);

        var moduleSetting = Optional.ofNullable(moduleElection.getValidationSetting()).orElse(new ValidationSetting());
        moduleElection.setValidationSetting(moduleSetting);

        moduleElection.setElectionValid(moduleElectionStatus.isValid());
        electionRepository.save(moduleElection);
        return createElectionTransferDTO(student, moduleElectionStatus, moduleElection, true);
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

    private ElectionTransferDTO createElectionTransferDTO(Student student, ModuleElectionStatus status,
                                                          ModuleElection moduleElection, boolean saved) {
        var electionStructure =
                new ModuleStructureGenerator(moduleDefinition, student, moduleElection, electionSemesters).generateStructure();

        return new ElectionTransferDTO(electionStructure,
                electionStatusMapper.toDto(status), saved, moduleElection.isElectionValid());
    }

    private ModuleElection loadModuleElectionForStudent(Student student) {
        return electionRepository.findModuleElectionByStudent(student.getEmail()).orElseGet(() -> {
                var moduleElection = new ModuleElection();
                moduleElection.setStudent(student);
                moduleElection.setValidationSetting(new ValidationSetting());
                moduleElection.setElectedModules(new HashSet<>());
                return moduleElection;
        });
    }
}
