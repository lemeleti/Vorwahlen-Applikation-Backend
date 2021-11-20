
package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.model.dto.ModuleElectionDTO;
import ch.zhaw.vorwahlen.model.dto.ModuleStructureDTO;
import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import ch.zhaw.vorwahlen.model.modules.Student;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleStructure;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleStructureFullTime;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleStructureGenerator;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleStructurePartTime;
import ch.zhaw.vorwahlen.modulevalidation.FullTimeElectionValidator;
import ch.zhaw.vorwahlen.modulevalidation.PartTimeElectionValidator;
import ch.zhaw.vorwahlen.repository.ElectionRepository;
import ch.zhaw.vorwahlen.repository.ModuleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Business logic for the election.
 */
@RequiredArgsConstructor
@Service
@Log
public class ElectionService {

    public static final int NUM_CONTEXT_MODULES = 3;
    public static final int NUM_SUBJECT_MODULES = 8;
    public static final int NUM_INTERDISCIPLINARY_MODULES = 1;

    private final ElectionRepository electionRepository;
    private final Function<Set<String>, Set<Module>> mapModuleSet;
    private final ModuleStructure structureFullTime;
    private final ModuleStructure structurePartTime;

    @Autowired
    public ElectionService(ElectionRepository electionRepository, ModuleRepository moduleRepository,
                           ModuleStructureFullTime structureFullTime, ModuleStructurePartTime structurePartTime) {
        this.electionRepository = electionRepository;
        this.mapModuleSet = list -> list.stream()
                                        .map(moduleRepository::getById)
                                        .collect(Collectors.toSet());
        this.structureFullTime = structureFullTime;
        this.structurePartTime = structurePartTime;
    }

    public ModuleStructureDTO getModuleStructure(Student student) {
        var structure = student.isTZ() ? structurePartTime : structureFullTime;
        ModuleElection election = null;
        if (electionRepository.existsById(student.getEmail())) {
            election = electionRepository.getById(student.getEmail());
        }
        return new ModuleStructureGenerator(structure, election, student).generateStructure();
    }

    /**
     * Gets the stored election from student.
     * @param student student in session
     * @return current election
     */
    public ModuleElectionDTO getModuleElectionByStudent(Student student) {
        var optional = electionRepository.findById(student.getEmail());
        if(optional.isPresent()) {
            return optional.map(DTOMapper.mapElectionToDto).get();
        }
        return null;
    }

    /**
     * Saves the election to the database.
     * @param student student in session
     * @param moduleElectionDTO his current election
     * @return true - if save successful<br>
     *         false - if arguments invalid
     */
    public ObjectNode saveElection(Student student, ModuleElectionDTO moduleElectionDTO) {
        if(student == null || moduleElectionDTO == null
                || student.getEmail() == null || student.getEmail().isBlank()) {
            return createSaveStatusBundle(false, false);
        }

        var moduleElection = DTOMapper.mapDtoToModuleElection(moduleElectionDTO, student, mapModuleSet);

        var electionValidator = student.isTZ()
                ? new PartTimeElectionValidator(student)
                : new FullTimeElectionValidator(student);

        var isValid = electionValidator.validate(moduleElection);
        moduleElection.setStudentEmail(student.getEmail());
        moduleElection.setElectionValid(isValid);
        moduleElectionDTO.setElectionValid(isValid); // needed in unit tests
        electionRepository.save(moduleElection);

        return createSaveStatusBundle(true, isValid);
    }

    private ObjectNode createSaveStatusBundle(boolean saveSuccess, boolean validElection) {
        var mapper = new ObjectMapper();
        var node = mapper.createObjectNode();
        node.put("electionSaved", saveSuccess);
        node.put("electionValid", validElection);
        return node;
    }
}
