package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.config.ResourceBundleMessageLoader;
import ch.zhaw.vorwahlen.exception.ImportException;
import ch.zhaw.vorwahlen.model.dto.StudentDTO;
import ch.zhaw.vorwahlen.model.dto.ValidationSettingDTO;
import ch.zhaw.vorwahlen.model.modules.Student;
import ch.zhaw.vorwahlen.parser.ClassListParser;
import ch.zhaw.vorwahlen.repository.ClassListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Business logic for the modules.
 */
@RequiredArgsConstructor
@Service
@Log
public class ClassListService {

    public static final int PA_DISPENSATION = 0;
    public static final int WPM_DISPENSATION = 0;
    private static final int YEAR_2_SHORT_YEAR = 100;

    private final ClassListRepository classListRepository;

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
            var message = String.format(ResourceBundleMessageLoader.getMessage("error.import_exception"), file.getOriginalFilename());
            throw new ImportException(message, e);
        }
    }

    public void setValidationSettings(ValidationSettingDTO validationSettingDTO, String userId) {
        var student = classListRepository.getById(userId);

        var validationSetting = student.getElection().getValidationSetting();
        validationSetting.setRepetent(validationSettingDTO.isRepetent());
        validationSetting.setSkipConsecutiveModuleCheck(validationSettingDTO.isSkipConsecutiveModuleCheck());
        validationSetting.setAlreadyElectedTwoConsecutiveModules(validationSettingDTO.hadAlreadyElectedTwoConsecutiveModules());

        classListRepository.save(student);
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
    public List<StudentDTO> getAllClassLists() {
        return classListRepository
                .findAll()
                .stream()
                .map(DTOMapper.mapStudentToDto)
                .toList();
    }

    /**
     * Get student from the database by id.
     * @param id the id for the student
     * @return Optional<StudentDTO>
     */
    public Optional<StudentDTO> getStudentById(String id) {
        return classListRepository
                .findById(id)
                .stream()
                .map(DTOMapper.mapStudentToDto)
                .findFirst();
    }

}
