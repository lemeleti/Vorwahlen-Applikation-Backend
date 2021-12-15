package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.config.ResourceBundleMessageLoader;
import ch.zhaw.vorwahlen.constants.ResourceMessageConstants;
import ch.zhaw.vorwahlen.exception.ModuleElectionConflictException;
import ch.zhaw.vorwahlen.exception.ModuleElectionNotFoundException;
import ch.zhaw.vorwahlen.exception.StudentNotFoundException;
import ch.zhaw.vorwahlen.exporter.ModuleElectionExporter;
import ch.zhaw.vorwahlen.mapper.Mapper;
import ch.zhaw.vorwahlen.model.dto.ElectionStatusDTO;
import ch.zhaw.vorwahlen.model.dto.ElectionTransferDTO;
import ch.zhaw.vorwahlen.model.dto.ModuleElectionDTO;
import ch.zhaw.vorwahlen.model.modules.ElectionSemesters;
import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import ch.zhaw.vorwahlen.model.modules.ModuleElectionStatus;
import ch.zhaw.vorwahlen.model.modules.Student;
import ch.zhaw.vorwahlen.model.modules.ValidationSetting;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleDefinition;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleStructureGenerator;
import ch.zhaw.vorwahlen.repository.ElectionRepository;
import ch.zhaw.vorwahlen.repository.ModuleRepository;
import ch.zhaw.vorwahlen.repository.StudentRepository;
import ch.zhaw.vorwahlen.validation.ElectionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static ch.zhaw.vorwahlen.constants.ResourceMessageConstants.ERROR_MODULE_ELECTION_NOT_FOUND;

/**
 * Business logic for the election.
 */
@Service
@Log
@RequiredArgsConstructor
public class ElectionService {
    private final ElectionRepository electionRepository;
    private final ModuleRepository moduleRepository;
    private final StudentRepository studentRepository;
    private final ElectionValidator electionValidator;
    private final ModuleDefinition moduleDefinition;
    private final ModuleElectionExporter exporter;
    private final ElectionSemesters electionSemesters;
    private final Mapper<ModuleElectionDTO, ModuleElection> moduleElectionMapper;
    private final Mapper<ElectionStatusDTO, ModuleElectionStatus> electionStatusMapper;

    @Qualifier("clientOutboundChannel")
    private final MessageChannel clientOutboundChannel;

    /**
     * Return all module elections
     * @return list of {@link ModuleElectionDTO}
     */
    public List<ModuleElectionDTO> getAllModuleElections() {
        return electionRepository.findAllModules()
                .stream()
                .map(moduleElectionMapper::toDto)
                .toList();
    }

    /**
     * Get module election by id
     * @param id identifier of the module election
     * @return ModuleElectionDTO
     */
    public ModuleElectionDTO getModuleElectionById(Long id) {
        return moduleElectionMapper.toDto(fetchModuleElectionById(id));
    }

    /**
     * Add new module election
     * @param moduleElectionDTO to be added module election
     */
    public ModuleElectionDTO createModuleElection(ModuleElectionDTO moduleElectionDTO) {
        if(electionRepository.existsById(moduleElectionDTO.getId())) {
            var formatString = ResourceBundleMessageLoader.getMessage(ResourceMessageConstants.ERROR_MODULE_ELECTION_CONFLICT);
            var message = String.format(formatString, moduleElectionDTO.getId());
            throw new ModuleElectionConflictException(message);
        }
        var election = moduleElectionMapper.toInstance(moduleElectionDTO);
        election = electionRepository.save(election);
        return moduleElectionMapper.toDto(election);
    }

    /**
     * Delete module election by id
     * @param id to be deleted module election
     */
    public void deleteModuleElectionById(Long id) {
        var moduleElection = fetchModuleElectionById(id);
        moduleElection.getStudent().setElection(null);
        moduleElection.setStudent(null);
        electionRepository.delete(moduleElection);
    }

    /**
     * Replace module election by id
     * @param id to be replaced module election
     * @param moduleElectionDTO new module election
     */
    public void updateModuleElection(Long id, ModuleElectionDTO moduleElectionDTO) {
        var savedModuleElection = fetchModuleElectionById(id);
        var newModuleElection = moduleElectionMapper.toInstance(moduleElectionDTO);
        newModuleElection.setId(savedModuleElection.getId());

        electionRepository.save(newModuleElection);
    }

    /**
     * Get election data for the specified user.
     * @param studentId get election for student
     * @return election data
     */
    public ElectionTransferDTO getElection(String studentId) {
        var student = fetchStudentById(studentId);
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
     * @param studentId student in session
     * @param moduleNo module that should be saved
     * @return ElectionTransferDTO containing the election data
     */
    public ElectionTransferDTO saveElection(String studentId, String moduleNo, SimpMessageHeaderAccessor headerAccessor) {
        var student = fetchStudentById(studentId);
        if(!student.isCanElect()) {
            var message = ResourceBundleMessageLoader.getMessage(ResourceMessageConstants.ERROR_ELECTION_CANNOT_ELECT);
            var newHeaderAccessor = StompHeaderAccessor.create(StompCommand.ERROR);

            newHeaderAccessor.setMessage(message);
            newHeaderAccessor.setSessionId(headerAccessor.getSessionId());
            newHeaderAccessor.setSessionAttributes(headerAccessor.getSessionAttributes());

            clientOutboundChannel.send(MessageBuilder.createMessage(new byte[0], newHeaderAccessor.getMessageHeaders()));
            return null;
        }

        var moduleElection = loadModuleElectionForStudent(student);
        migrateElectionChanges(moduleElection, moduleNo);

        var moduleSetting = Optional.ofNullable(moduleElection.getValidationSetting()).orElse(new ValidationSetting());
        moduleElection.setValidationSetting(moduleSetting);

        var moduleElectionStatus = electionValidator.validate(moduleElection);
        moduleElection.setElectionValid(moduleElectionStatus.isValid());
        electionRepository.save(moduleElection);
        return createElectionTransferDTO(student, moduleElectionStatus, moduleElection, true);
    }

    /**
     * Validate the election again.
     * @param studentId email of the student.
     */
    public void updateValidation(String studentId) {
        var student = fetchStudentById(studentId);
        var moduleElection = loadModuleElectionForStudent(student);

        var moduleSetting = Optional.ofNullable(moduleElection.getValidationSetting()).orElse(new ValidationSetting());
        moduleElection.setValidationSetting(moduleSetting);

        var moduleElectionStatus = electionValidator.validate(moduleElection);
        moduleElection.setElectionValid(moduleElectionStatus.isValid());
        electionRepository.save(moduleElection);
    }

    private Student fetchStudentById(String studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> {
                    var formatString =
                            ResourceBundleMessageLoader.getMessage(ResourceMessageConstants.ERROR_STUDENT_NOT_FOUND);
                    return new StudentNotFoundException(String.format(formatString, studentId));
                });
    }

    /**
     * Export all module elections.
     * @return byte array containing the formatted module election.
     */
    public byte[] exportModuleElection() {
        return exporter.export(electionRepository.findAllModules());
    }

    private void migrateElectionChanges(ModuleElection moduleElection, String moduleNo) {
        var module = moduleRepository.findById(moduleNo).orElseThrow();
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

    private ModuleElection fetchModuleElectionById(Long id) {
        return electionRepository.findModuleElectionById(id).orElseThrow(() -> {
            var resourceMessage = ResourceBundleMessageLoader.getMessage(ERROR_MODULE_ELECTION_NOT_FOUND);
            var errorMessage = String.format(resourceMessage, id);
            return new ModuleElectionNotFoundException(errorMessage);
        });
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
