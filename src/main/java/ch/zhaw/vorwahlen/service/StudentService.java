package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.config.ResourceBundleMessageLoader;
import ch.zhaw.vorwahlen.constants.ResourceMessageConstants;
import ch.zhaw.vorwahlen.exception.ImportException;
import ch.zhaw.vorwahlen.exception.StudentNotFoundException;
import ch.zhaw.vorwahlen.mapper.Mapper;
import ch.zhaw.vorwahlen.model.dto.NotificationDTO;
import ch.zhaw.vorwahlen.model.dto.StudentDTO;
import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import ch.zhaw.vorwahlen.model.modules.Student;
import ch.zhaw.vorwahlen.model.modules.StudentClass;
import ch.zhaw.vorwahlen.model.modules.ValidationSetting;
import ch.zhaw.vorwahlen.parser.ClassListParser;
import ch.zhaw.vorwahlen.parser.DispensationParser;
import ch.zhaw.vorwahlen.repository.StudentClassRepository;
import ch.zhaw.vorwahlen.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Business logic for the modules.
 */
@RequiredArgsConstructor
@Service
@Log
public class StudentService {

    public static final int PA_DISPENSATION = 0;
    public static final int WPM_DISPENSATION = 0;
    private static final int YEAR_2_SHORT_YEAR = 100;

    private final StudentRepository studentRepository;
    private final StudentClassRepository studentClassRepository;
    private final JavaMailSender emailSender;

    private final Mapper<StudentDTO, Student> mapper;

    /**
     * Importing the Excel file and storing the needed content into the database.
     * @param file the Excel file to be parsed and stored.
     */
    public void importClassListExcel(MultipartFile file, String worksheet) {
        try {
            var classListParser = new ClassListParser(file.getInputStream(), worksheet);
            var students = classListParser.parseFromXLSX();
            setSecondElection(students);
            createAndSetModuleElection(students);
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
        var student = mapper.toInstance(studentDTO);
        var moduleElection = new ModuleElection();
        var validationSetting = new ValidationSetting();
        var studentClass = getOrCreateStudentClass(studentDTO.getClazz());

        moduleElection.setStudent(student);
        moduleElection.setValidationSetting(validationSetting);

        student.setElection(moduleElection);
        student.setStudentClass(studentClass);

        return mapper.toDto(studentRepository.save(student));
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

    private void createAndSetModuleElection(List<Student> students) {
        students.forEach(student -> {
                    var moduleElection = new ModuleElection();
                    var validationSetting = new ValidationSetting();

                    moduleElection.setStudent(student);
                    moduleElection.setValidationSetting(validationSetting);
                    moduleElection.setElectedModules(new HashSet<>());

                    student.setElection(moduleElection);
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
                isSecondElection = Math.abs(shortYear - Integer.parseInt(parsedYear)) > 2;
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
                .map(mapper::toDto)
                .toList();
    }

    /**
     * Get student from the database by id.
     * @param id the id for the student
     * @return Optional<StudentDTO>
     */
    public StudentDTO getStudentById(String id) {
        return mapper.toDto(fetchStudentById(id));
    }

    /**
     * Delete student by id
     * @param id identifier of the student.
     */
    public void deleteStudentById(String id) {
        if(!studentRepository.existsById(id)) {
            var formatString = ResourceBundleMessageLoader.getMessage(ResourceMessageConstants.ERROR_STUDENT_NOT_FOUND);
            throw new StudentNotFoundException(String.format(formatString, id));
        }
        studentRepository.deleteById(id);
    }

    /**
     * Replace a student by id.
     * @param id identifier of the student to be replaced.
     * @param studentDTO new student.
     * @return the saved student
     */
    public StudentDTO replaceStudent(String id, StudentDTO studentDTO) {
        studentDTO.setEmail(id);

        var studentClass = getOrCreateStudentClass(studentDTO.getClazz());
        var storedStudent = studentRepository.findById(id);
        if(storedStudent.isPresent()) {
            var newStudent = mapper.toInstance(studentDTO);
            newStudent.setStudentClass(studentClass);
            return mapper.toDto(studentRepository.save(newStudent));
        } else {
            return addStudent(studentDTO);
        }
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

        if (patchedFields.containsKey(ip)) {
            student.setIP(patchedFields.get(ip));
        }

        if (patchedFields.containsKey(firstTimeSetup)) {
            student.setFirstTimeSetup(patchedFields.get(firstTimeSetup));
        }

        studentRepository.save(student);
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
        var messages = new SimpleMailMessage[notificationDTO.studentMailAddresses().length];
        var emailSenderImpl = (JavaMailSenderImpl) emailSender;
        emailSenderImpl.setUsername(notificationDTO.email());
        emailSenderImpl.setPassword(notificationDTO.password());

        for (var i = 0; i < studentMailAddresses.length; i++) {
             messages[i] = createSimpleMailMessage(studentMailAddresses[i], notificationDTO);
        }
        emailSenderImpl.send(messages);
    }

    private SimpleMailMessage createSimpleMailMessage(String recipient, NotificationDTO notificationDTO) {
        var message = new SimpleMailMessage();
        message.setFrom(notificationDTO.email());
        message.setTo(recipient);
        message.setSubject(notificationDTO.subject());
        message.setText(notificationDTO.message());
        return message;
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
