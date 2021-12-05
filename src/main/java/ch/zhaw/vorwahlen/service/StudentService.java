package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.config.ResourceBundleMessageLoader;
import ch.zhaw.vorwahlen.constants.ResourceMessageConstants;
import ch.zhaw.vorwahlen.exception.ImportException;
import ch.zhaw.vorwahlen.exception.StudentNotFoundException;
import ch.zhaw.vorwahlen.mapper.Mapper;
import ch.zhaw.vorwahlen.model.dto.StudentDTO;
import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import ch.zhaw.vorwahlen.model.modules.Student;
import ch.zhaw.vorwahlen.model.modules.StudentClass;
import ch.zhaw.vorwahlen.model.modules.ValidationSetting;
import ch.zhaw.vorwahlen.parser.ClassListParser;
import ch.zhaw.vorwahlen.parser.DispensationParser;
import ch.zhaw.vorwahlen.repository.ClassListRepository;
import ch.zhaw.vorwahlen.repository.StudentClassRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;
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

    private final ClassListRepository classListRepository;
    private final StudentClassRepository studentClassRepository;

    private final Mapper<StudentDTO, Student> mapper;

    /**
     * Importing the Excel file and storing the needed content into the database.
     * @param file the Excel file to be parsed and stored.
     */
    public void importClassListExcel(MultipartFile file, String worksheet) {
        try {
            var classListParser = new ClassListParser(file.getInputStream(), worksheet);
            var classLists = classListParser.parseModulesFromXLSX();
            setSecondElection(classLists);
            classListRepository.saveAll(classLists);
        } catch (IOException e) {
            var formatString = ResourceBundleMessageLoader.getMessage(ResourceMessageConstants.ERROR_IMPORT_EXCEPTION);
            var message = String.format(formatString, file.getOriginalFilename());
            throw new ImportException(message, e);
        }
    }

    private Student addStudent(StudentDTO studentDTO) {
        var student = mapper.toInstance(studentDTO);
        var moduleElection = new ModuleElection();
        var validationSetting = new ValidationSetting();
        var studentClass = getOrCreateStudentClass(studentDTO.getClazz());

        moduleElection.setStudent(student);
        moduleElection.setValidationSetting(validationSetting);

        student.setElection(moduleElection);
        student.setStudentClass(studentClass);

        return classListRepository.save(student);
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

    public URI addAndReturnLocation(StudentDTO studentDTO) {
        var addedStudent = addStudent(studentDTO);
        try {
            return new URI("/students/".concat(addedStudent.getEmail()));
        } catch (URISyntaxException e) {
            throw new RuntimeException();
        }
    }

    private void setSecondElection(List<Student> students) {
        students.stream()
                .filter(Student::isTZ)
                .forEach(student-> student.setSecondElection(isSecondElection(student.getStudentClass().getName())));
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
     * Get all class lists from the database.
     * @return a list of {@link StudentDTO}.
     */
    public List<StudentDTO> getAllStudents() {
        return classListRepository
                .findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<StudentDTO> getAllStudentsByElection(boolean electionStatus) {
        return classListRepository.getAllByElectionStatus(electionStatus)
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
        return classListRepository
                .findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> {
                    var formatString =
                            ResourceBundleMessageLoader.getMessage(ResourceMessageConstants.ERROR_STUDENT_NOT_FOUND);
                    return new StudentNotFoundException(String.format(formatString, id));
                });
    }

    public void deleteStudentById(String id) {

        classListRepository.deleteById(id);
    }

    public StudentDTO replaceStudent(String id, StudentDTO studentDTO) {
        var studentClass = getOrCreateStudentClass(studentDTO.getClazz());
        var updatedStudent = classListRepository.findById(id)
                .map(student -> {
                    student.setName(studentDTO.getName());
                    student.setStudentClass(studentClass);
                    student.setPaDispensation(studentDTO.getPaDispensation());
                    student.setWpmDispensation(studentDTO.getWpmDispensation());
                    student.setSecondElection(studentDTO.isSecondElection());
                    return classListRepository.save(student);
                })
                .orElse(addStudent(studentDTO));

        return mapper.toDto(updatedStudent);
    }

    /**
     * Importing the Excel file and storing the needed content into the database.
     * @param file the Excel file to be parsed and stored.
     */
    public void importDispensationExcel(MultipartFile file, String worksheet) {
        try (InputStream is = file.getInputStream()) {
            var dispensationParser = new DispensationParser(is, worksheet);
            var parsedList = dispensationParser.parseModulesFromXLSX();
            parsedList.forEach(student -> classListRepository.findById(student.getEmail()).ifPresent(dbStudent -> {
                dbStudent.setPaDispensation(student.getPaDispensation());
                dbStudent.setWpmDispensation(student.getWpmDispensation());
                classListRepository.save(dbStudent);
            }));
        } catch (IOException e) {
            var formatString = ResourceBundleMessageLoader.getMessage(ResourceMessageConstants.ERROR_IMPORT_EXCEPTION);
            var message = String.format(formatString, file.getOriginalFilename());
            throw new ImportException(message, e);
        }
    }
}
