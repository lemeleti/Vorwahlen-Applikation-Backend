package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.config.ResourceBundleMessageLoader;
import ch.zhaw.vorwahlen.config.UserBean;
import ch.zhaw.vorwahlen.constants.ResourceMessageConstants;
import ch.zhaw.vorwahlen.exception.ElectionConflictException;
import ch.zhaw.vorwahlen.exception.ElectionNotFoundException;
import ch.zhaw.vorwahlen.exception.StudentNotFoundException;
import ch.zhaw.vorwahlen.exporter.ElectionExporter;
import ch.zhaw.vorwahlen.mapper.Mapper;
import ch.zhaw.vorwahlen.model.core.election.ElectionDTO;
import ch.zhaw.vorwahlen.model.core.election.ElectionStatus;
import ch.zhaw.vorwahlen.model.core.election.ElectionStatusDTO;
import ch.zhaw.vorwahlen.model.ElectionTransferDTO;
import ch.zhaw.vorwahlen.model.modulestructure.ElectionSemesters;
import ch.zhaw.vorwahlen.model.core.election.Election;
import ch.zhaw.vorwahlen.model.core.student.Student;
import ch.zhaw.vorwahlen.model.core.validationsetting.ValidationSetting;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleDefinition;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleStructureGenerator;
import ch.zhaw.vorwahlen.repository.ElectionRepository;
import ch.zhaw.vorwahlen.repository.ModuleRepository;
import ch.zhaw.vorwahlen.repository.StudentRepository;
import ch.zhaw.vorwahlen.validation.ElectionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import static ch.zhaw.vorwahlen.constants.ResourceMessageConstants.*;

/**
 * Business logic for the election.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ElectionService {
    private final ElectionRepository electionRepository;
    private final ModuleRepository moduleRepository;
    private final StudentRepository studentRepository;
    private final ElectionValidator electionValidator;
    private final ModuleDefinition moduleDefinition;
    private final ElectionExporter exporter;
    private final ElectionSemesters electionSemesters;
    private final Mapper<ElectionDTO, Election>  electionMapper;
    private final Mapper<ElectionStatusDTO, ElectionStatus> electionStatusMapper;
    private final UserBean userBean;

    @Qualifier("clientOutboundChannel")
    private final MessageChannel clientOutboundChannel;

    /**
     * Return all  elections
     * @return list of {@link ElectionDTO}
     */
    public List<ElectionDTO> getAllElections() {
        return electionRepository.findAllModules()
                .stream()
                .map( electionMapper::toDto)
                .toList();
    }

    /**
     * Get  election by id
     * @param id identifier of the  election
     * @return ElectionDTO
     */
    public ElectionDTO getElectionById(Long id) {
        return  electionMapper.toDto(fetchElectionById(id));
    }

    /**
     * Add new  election
     * @param electionDTO to be added  election
     */
    public ElectionDTO createElection(ElectionDTO electionDTO) {
        userBean.getUserFromSecurityContext().ifPresent(user ->
                log.debug("User: {} requested to create  election: {}", user, electionDTO)
        );
        if(electionRepository.existsById(electionDTO.getId())) {
            log.debug("Throwing ModuleConflictException because  election with id {} already exists", electionDTO.getId());
            var formatString = ResourceBundleMessageLoader.getMessage(ResourceMessageConstants.ERROR_MODULE_ELECTION_CONFLICT);
            var message = String.format(formatString, electionDTO.getId());
            throw new ElectionConflictException(message);
        }
        var election =  electionMapper.toInstance(electionDTO);
        election = electionRepository.save(election);
        log.debug("Election: {} was saved successfully to the database", election);
        return  electionMapper.toDto(election);
    }

    /**
     * Delete  election by id
     * @param id to be deleted  election
     */
    public void deleteElectionById(Long id) {
        userBean.getUserFromSecurityContext().ifPresent(user ->
            log.debug("User: {} requested to delete  election with id: {}", user.getMail(), id)
        );
        var  election = fetchElectionById(id);
         election.getStudent().setElection(null);
         election.setStudent(null);
        electionRepository.delete( election);
        log.debug(" election was deleted successfully");
    }

    /**
     * Replace  election by id
     * @param id to be replaced  election
     * @param electionDTO new  election
     */
    public void updateElection(Long id, ElectionDTO electionDTO) {

        var savedElection = fetchElectionById(id);
        var newElection =  electionMapper.toInstance(electionDTO);
        newElection.setId(savedElection.getId());
        userBean.getUserFromSecurityContext().ifPresent(user ->
                log.debug("User: {} requested to update  election {} with {}",
                        user.getMail(), savedElection, newElection)
        );
        electionRepository.save(newElection);
        log.debug(" election was successfully updated");
    }

    /**
     * Get election data for the specified user.
     * @param studentId get election for student
     * @return election data
     */
    public ElectionTransferDTO getElection(String studentId) {
        var student = fetchStudentById(studentId);
        var  election = loadElectionForStudent(student);
        var  electionStatus = electionValidator.validate( election);
        return createElectionTransferDTO(student,  electionStatus,  election, false);
    }

    /**
     * Get election for the specified user
     * @param student get election for student
     * @return  election from db if existent otherwise a new instance will be created.
     */
    public ElectionDTO getElectionForStudent(Student student) {
        return  electionMapper.toDto(loadElectionForStudent(student));
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

        var  election = loadElectionForStudent(student);
        migrateElectionChanges( election, moduleNo);

        var moduleSetting = Optional.ofNullable( election.getValidationSetting()).orElse(new ValidationSetting());
         election.setValidationSetting(moduleSetting);

        var  electionStatus = electionValidator.validate( election);
         election.setElectionValid( electionStatus.isValid());
        electionRepository.save( election);
        return createElectionTransferDTO(student,  electionStatus,  election, true);
    }

    /**
     * Validate the election again.
     * @param studentId email of the student.
     */
    public void updateValidation(String studentId) {
        var student = fetchStudentById(studentId);
        var  election = loadElectionForStudent(student);

        var moduleSetting = Optional.ofNullable( election.getValidationSetting()).orElse(new ValidationSetting());
         election.setValidationSetting(moduleSetting);

        var  electionStatus = electionValidator.validate( election);
         election.setElectionValid( electionStatus.isValid());
        electionRepository.save( election);
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
     * Export all  elections.
     * @return byte array containing the formatted  election.
     */
    public byte[] exportElection() {
        return exporter.export(electionRepository.findAllModules());
    }

    /**
     * Close the election for all students
     */
    public void closeElection() {
        studentRepository.closeElection();
    }

    private void migrateElectionChanges(Election election, String moduleNo) {
        var module = moduleRepository.findById(moduleNo).orElseThrow();
        var electedModules = election.getElectedModules();
        if (!electedModules.removeIf(m -> moduleNo.equals(m.getModuleNo()))) {
            electedModules.add(module);
        }
    }

    private ElectionTransferDTO createElectionTransferDTO(Student student, ElectionStatus status,
                                                          Election election, boolean saved) {
        var electionStructure =
                new ModuleStructureGenerator(moduleDefinition, student, election, electionSemesters).generateStructure();

        return new ElectionTransferDTO(electionStructure,
                                       electionStatusMapper.toDto(status), saved, election.isElectionValid());
    }

    private Election fetchElectionById(Long id) {
        return electionRepository.findElectionById(id).orElseThrow(() -> {
            var resourceMessage = ResourceBundleMessageLoader.getMessage(ERROR_MODULE_ELECTION_NOT_FOUND);
            var errorMessage = String.format(resourceMessage, id);
            return new ElectionNotFoundException(errorMessage);
        });
    }

    private Election loadElectionForStudent(Student student) {
        return electionRepository.findElectionByStudent(student.getEmail()).orElseGet(() -> {
                var  election = new Election();
                 election.setStudent(student);
                 election.setValidationSetting(new ValidationSetting());
                 election.setElectedModules(new HashSet<>());
                return  election;
        });
    }
}
