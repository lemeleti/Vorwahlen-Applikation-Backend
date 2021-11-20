
package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.model.dto.ModuleElectionDTO;
import ch.zhaw.vorwahlen.model.dto.ModuleStructureDTO;
import ch.zhaw.vorwahlen.model.dto.StudentDTO;
import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleCategory;
import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleStructure;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleStructureFullTime;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleStructureGenerator;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleStructurePartTime;
import ch.zhaw.vorwahlen.modulevalidation.AbstractElectionValidator;
import ch.zhaw.vorwahlen.modulevalidation.ElectionValidator;
import ch.zhaw.vorwahlen.repository.ElectionRepository;
import ch.zhaw.vorwahlen.repository.ModuleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    public static final int MAX_CREDITS_PER_YEAR_WITHOUT_PA_AND_BA = 42; // PA = 6 Credits, BA = 12 Credits
    public static final int NUM_CONTEXT_MODULES = 3;
    public static final int NUM_SUBJECT_MODULES = 8;
    public static final int NUM_INTERDISCIPLINARY_MODULES = 1;
    public static final int NUM_ENGLISH_CREDITS = 20;
    public static final int CREDIT_PER_SUBJECT_MODULE = 4;

    private final ElectionRepository electionRepository;
    private final Function<Set<String>, Set<Module>> mapModuleSet;
    private final ModuleStructureFullTime structureFullTime;
    private final ModuleStructurePartTime structurePartTime;
    private final ElectionValidator electionValidator;

    @Autowired
    public ElectionService(ElectionRepository electionRepository, ModuleRepository moduleRepository,
                           ModuleStructureFullTime structureFullTime, ModuleStructurePartTime structurePartTime,
                           ElectionValidator electionValidator) {
        this.electionRepository = electionRepository;
        this.mapModuleSet = list -> list.stream()
                                        .map(moduleRepository::getById)
                                        .collect(Collectors.toSet());
        this.structureFullTime = structureFullTime;
        this.structurePartTime = structurePartTime;
        this.electionValidator = electionValidator;
    }

    public ModuleStructureDTO getModuleStructure(StudentDTO student) {
        ModuleStructure structure = student.isTZ() ? structurePartTime : structureFullTime;
        ModuleElection election = null;
        if (electionRepository.existsById(student.getEmail())) {
            election = electionRepository.getById(student.getEmail());
        }
        return new ModuleStructureGenerator(structure, election, student).generateStructure();
    }

    /**
     * Gets the stored election from student.
     * @param studentDTO student in session
     * @return current election
     */
    public ModuleElectionDTO getModuleElectionByStudent(StudentDTO studentDTO) {
        var optional = electionRepository.findById(studentDTO.getEmail());
        if(optional.isPresent()) {
            return optional.map(DTOMapper.mapElectionToDto).get();
        }
        return null;
    }

    /**
     * Saves the election to the database.
     * @param studentDTO student in session
     * @param moduleElectionDTO his current election
     * @return true - if save successful<br>
     *         false - if arguments invalid
     */
    public ObjectNode saveElection(StudentDTO studentDTO, ModuleElectionDTO moduleElectionDTO) {
        // optional todo: test double modules like MC1/MC2 (not one missing)
        if(studentDTO == null || moduleElectionDTO == null
                || studentDTO.getEmail() == null || studentDTO.getEmail().isBlank()) {
            return createSaveStatusBundle(false, false);
        }
        var moduleElection = DTOMapper.mapDtoToModuleElection(moduleElectionDTO, studentDTO, mapModuleSet);

        var isValid = electionValidator.validate(moduleElection);
        moduleElection.setStudentEmail(studentDTO.getEmail());
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
