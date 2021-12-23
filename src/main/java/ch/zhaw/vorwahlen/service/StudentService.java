package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.config.ResourceBundleMessageLoader;
import ch.zhaw.vorwahlen.config.UserBean;
import ch.zhaw.vorwahlen.constants.ResourceMessageConstants;
import ch.zhaw.vorwahlen.exception.ImportException;
import ch.zhaw.vorwahlen.exception.MailMessagingException;
import ch.zhaw.vorwahlen.exception.ElectionNotFoundException;
import ch.zhaw.vorwahlen.exception.StudentConflictException;
import ch.zhaw.vorwahlen.exception.StudentNotFoundException;
import ch.zhaw.vorwahlen.exception.ValidationSettingNotFoundException;
import ch.zhaw.vorwahlen.mapper.Mapper;
import ch.zhaw.vorwahlen.model.mailtemplate.NotificationDTO;
import ch.zhaw.vorwahlen.model.core.student.StudentDTO;
import ch.zhaw.vorwahlen.model.core.validationsetting.ValidationSettingDTO;
import ch.zhaw.vorwahlen.model.core.election.Election;
import ch.zhaw.vorwahlen.model.core.student.Student;
import ch.zhaw.vorwahlen.model.core.student.StudentClass;
import ch.zhaw.vorwahlen.model.core.validationsetting.ValidationSetting;
import ch.zhaw.vorwahlen.parser.ClassListParser;
import ch.zhaw.vorwahlen.parser.DispensationParser;
import ch.zhaw.vorwahlen.repository.ElectionRepository;
import ch.zhaw.vorwahlen.repository.StudentClassRepository;
import ch.zhaw.vorwahlen.repository.StudentRepository;
import ch.zhaw.vorwahlen.repository.ValidationSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static ch.zhaw.vorwahlen.constants.ResourceMessageConstants.*;

/**
 * Business logic for the modules.
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class StudentService {

    public static final int PA_DISPENSATION = 0;
    public static final int WPM_DISPENSATION = 0;
    private static final int YEAR_2_SHORT_YEAR = 100;

    private final StudentRepository studentRepository;
    private final StudentClassRepository studentClassRepository;
    private final ElectionRepository electionRepository;
    private final ValidationSettingRepository validationSettingRepository;
    private final JavaMailSender emailSender;

    private final Mapper<StudentDTO, Student> studentMapper;
    private final Mapper<ValidationSettingDTO, ValidationSetting> validationSettingMapper;

    private final UserBean userBean;

    /**
     * Importing the Excel file and storing the needed content into the database.
     * @param file the Excel file to be parsed and stored.
     */
    public void importClassListExcel(MultipartFile file, String worksheet) {
        try {
            var classListParser = new ClassListParser(file.getInputStream(), worksheet);
            var students = classListParser.parseFromXLSX();
            setSecondElection(students);
            createAndSetElection(students);
            studentRepository.saveAll(students);
        } catch (IOException e) {
            var formatString = ResourceBundleMessageLoader.getMessage(ResourceMessageConstants.ERROR_IMPORT_EXCEPTION);
            var message = String.format(formatString, file.getOriginalFilename());
            throw new ImportException(message, e);
        }
    }
    /**
     * Add student.
     * @param studentDTO to be added student.
     * @return the added student
     */
    public StudentDTO addStudent(StudentDTO studentDTO) {
        userBean.getUserFromSecurityContext().ifPresent(user ->
            log.debug("User: {} requested to add a student: {}", user.getMail(), studentDTO)
        );
        if(studentRepository.existsById(studentDTO.getEmail())) {
            log.debug("Throwing StudentConflictException because student with email {} already exists", studentDTO.getEmail());
            var formatString = ResourceBundleMessageLoader.getMessage(ERROR_STUDENT_CONFLICT);
            var message = String.format(formatString, studentDTO.getEmail());
            throw new StudentConflictException(message);
        }
        var student = studentMapper.toInstance(studentDTO);
        var  election = new Election();
        var validationSetting = new ValidationSetting();
        var studentClass = getOrCreateStudentClass(studentDTO.getClazz());

         election.setStudent(student);
         election.setValidationSetting(validationSetting);

        student.setElection( election);
        student.setStudentClass(studentClass);

        student = studentRepository.save(student);
        log.debug("Student: {} was saved successfully to the database", student);
        return studentMapper.toDto(student);
    }

    private StudentClass getOrCreateStudentClass(String className) {
        return studentClassRepository
                .findById(className)
                .orElseGet(() -> {
                    var sc = new StudentClass();
                    sc.setName(className);
                    return sc;
                });
    }

    private void setSecondElection(List<Student> students) {
        students.stream()
                .filter(Student::isTZ)
                .forEach(student-> student.setSecondElection(isSecondElection(student.getStudentClass().getName())));
    }

    private void createAndSetElection(List<Student> students) {
        students.forEach(student -> {
                    var  election = new Election();
                    var validationSetting = new ValidationSetting();

                     election.setStudent(student);
                     election.setValidationSetting(validationSetting);
                     election.setElectedModules(new HashSet<>());

                    student.setElection( election);
                });
    }

    private boolean isSecondElection(String clazz) {
        var isSecondElection = false;
        var shortYear = LocalDate.now().getYear() % YEAR_2_SHORT_YEAR;

        var pattern = Pattern.compile(ClassListParser.TZ_CLASS_REGEX);
        var matcher = pattern.matcher(clazz);
        if (matcher.find()) {
            var parsedYear = matcher.group("year");
            if (parsedYear != null && !parsedYear.isBlank()) {
                isSecondElection = Math.abs(shortYear - Integer.parseInt(parsedYear)) >= 2;
            }
        }

        return isSecondElection;
    }

    /**
     * Get all students from the database.
     * @return a list of {@link StudentDTO}.
     */
    public List<StudentDTO> getAllStudents() {
        return studentRepository
                .findAll()
                .stream()
                .map(studentMapper::toDto)
                .toList();
    }

    /**
     * Get student from the database by id.
     * @param id the id for the student
     * @return Optional<StudentDTO>
     */
    public StudentDTO getStudentById(String id) {
        return studentMapper.toDto(fetchStudentById(id));
    }

    /**
     * Delete student by id
     * @param id identifier of the student.
     */
    public void deleteStudentById(String id) {
        userBean.getUserFromSecurityContext().ifPresent(user ->
            log.debug("User: {} requested to delete a student with email: {}", user.getMail(), id)
        );
        var student = fetchStudentById(id);
        studentRepository.deleteById(student.getEmail());
        log.debug("student was deleted successfully");
    }

    /**
     * Replace a student by id.
     * @param id identifier of the student to be replaced.
     * @param studentDTO new student.
     * @return the saved student
     */
    public StudentDTO replaceStudent(String id, StudentDTO studentDTO) {
        var studentClass = getOrCreateStudentClass(studentDTO.getClazz());
        var election = electionRepository.findElectionById(studentDTO.getModuleElectionId())
                .orElseThrow(() -> {
                    var resourceMessage = ResourceBundleMessageLoader.getMessage(ERROR_MODULE_ELECTION_NOT_FOUND);
                    var errorMessage = String.format(resourceMessage, studentDTO.getModuleElectionId());
                    return new ElectionNotFoundException(errorMessage);
                });

        var storedStudent = fetchStudentById(id);
        var newStudent = studentMapper.toInstance(studentDTO);
        newStudent.setEmail(storedStudent.getEmail());
        newStudent.setStudentClass(studentClass);
        newStudent.setElection(election);
        userBean.getUserFromSecurityContext().ifPresent(user ->
            log.debug("User: {} requested to update student {} with {}",
                      user.getMail(), storedStudent, newStudent)
        );
        var student = studentRepository.save(newStudent);
        log.debug("student was successfully updated");
        return studentMapper.toDto(student);
    }

    /**
     * Get validation setting for student by id
     * @param id mail of the student
     * @return {@link ValidationSettingDTO}
     */
    public ValidationSettingDTO getValidationSettingForStudent(String id) {
        var setting = validationSettingRepository.findValidationSettingByStudentMail(id).orElseThrow(() -> {
            var errorMessage =
                    String.format(ResourceBundleMessageLoader.getMessage(ERROR_VALIDATION_SETTING_NOT_FOUND), id);
            return new ValidationSettingNotFoundException(errorMessage);
        });
        return validationSettingMapper.toDto(setting);
    }

    /**
     * Replace the settings by student by id.
     * @param studentId identifier of the student to be replaced.
     * @param validationSettingDTO new validation settings.
     */
    public void replaceValidationSettings(String studentId, ValidationSettingDTO validationSettingDTO) {
        var storedStudent = fetchStudentById(studentId);
        var validationSettings = validationSettingMapper.toInstance(validationSettingDTO);

        if(storedStudent.getElection() != null && storedStudent.getElection().getValidationSetting() != null) {
            validationSettings.setId(storedStudent.getElection().getValidationSetting().getId());
        }

        userBean.getUserFromSecurityContext().ifPresent(user ->
            log.debug("User: {} requested to update validation setting {} with {}",
                      user.getMail(), storedStudent.getElection().getValidationSetting(), validationSettings)
        );
        validationSettingRepository.save(validationSettings);
        log.debug("validation setting was successfully updated.");
    }

    /**
     * Patch student informations for a student.
     * @param id identifier of the student
     * @param patchedFields map of fields to be updated and it's new values.
     */
    public void updateStudentEditableFields(String id, Map<String, Boolean> patchedFields) {
        var student = fetchStudentById(id);
        var ip = "ip";
        var firstTimeSetup = "firstTimeSetup";

        userBean.getUserFromSecurityContext().ifPresent(user ->
            log.debug("User: {} requested to patch student values {} with {}",
                      user.getMail(), student, patchedFields)
        );

        if (patchedFields.containsKey(ip)) {
            student.setIP(patchedFields.get(ip));
        }

        if (patchedFields.containsKey(firstTimeSetup)) {
            student.setFirstTimeSetup(patchedFields.get(firstTimeSetup));
        }
        studentRepository.save(student);
        log.debug("student values patched successfully");
    }

    /**
     * Importing the Excel file and storing the needed content into the database.
     * @param file the Excel file to be parsed and stored.
     */
    public void importDispensationExcel(MultipartFile file, String worksheet) {
        try (InputStream is = file.getInputStream()) {
            var dispensationParser = new DispensationParser(is, worksheet);
            var parsedList = dispensationParser.parseFromXLSX();
            parsedList.forEach(student -> studentRepository.findById(student.getEmail()).ifPresent(dbStudent -> {
                dbStudent.setPaDispensation(student.getPaDispensation());
                dbStudent.setWpmDispensation(student.getWpmDispensation());
                studentRepository.save(dbStudent);
            }));
        } catch (IOException e) {
            var formatString = ResourceBundleMessageLoader.getMessage(ResourceMessageConstants.ERROR_IMPORT_EXCEPTION);
            var message = String.format(formatString, file.getOriginalFilename());
            throw new ImportException(message, e);
        }
    }

    /**
     * Notify students per email.
     * @param notificationDTO the notification to be sent.
     */
    public void notifyStudents(NotificationDTO notificationDTO) {
        var studentMailAddresses = notificationDTO.studentMailAddresses();
        var messages = new MimeMessage[notificationDTO.studentMailAddresses().length];
        var emailSenderImpl = (JavaMailSenderImpl) emailSender;
        emailSenderImpl.setUsername(notificationDTO.email());
        emailSenderImpl.setPassword(notificationDTO.password());

        for (var i = 0; i < studentMailAddresses.length; i++) {
            var message = emailSenderImpl.createMimeMessage();
            prepareMail(message, studentMailAddresses[i], notificationDTO);
            messages[i] = message;
        }

        emailSenderImpl.send(messages);
    }

    private void prepareMail(MimeMessage mimeMessage, String recipient, NotificationDTO notificationDTO) {
        try {
            var helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setFrom(notificationDTO.email());
            helper.setTo(recipient);
            helper.setSubject(notificationDTO.subject());
            helper.setText(notificationDTO.message(), true);
        } catch (MessagingException e) {
            throw new MailMessagingException(e.getLocalizedMessage());
        }
    }

    private Student fetchStudentById(String id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> {
                    var formatString =
                            ResourceBundleMessageLoader.getMessage(ResourceMessageConstants.ERROR_STUDENT_NOT_FOUND);
                    return new StudentNotFoundException(String.format(formatString, id));
                });

    }
}
