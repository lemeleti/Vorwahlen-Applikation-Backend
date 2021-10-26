package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.model.dto.StudentDTO;
import ch.zhaw.vorwahlen.model.modules.Student;
import ch.zhaw.vorwahlen.parser.ClassListParser;
import ch.zhaw.vorwahlen.repository.ClassListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Business logic for the modules.
 */
@RequiredArgsConstructor
@Service
public class ClassListService {

    private final Logger logger = Logger.getLogger(ClassListService.class.getName());

    public static final int PA_DISPENSATION = 0;
    public static final int WPM_DISPENSATION = 0;

    private final ClassListRepository classListRepository;

    /**
     * Importing the Excel file and storing the needed content into the database.
     * @param file the Excel file to be parsed and stored.
     */
    public void importClassListExcel(MultipartFile file, String worksheet) {
        try {
            var classListParser = new ClassListParser(file.getInputStream(), worksheet);
            var classLists = classListParser.parseModulesFromXLSX();
            classListRepository.saveAll(classLists);
        } catch (IOException e) {
            var message = String.format("Die Datei %s konnte nicht abgespeichert werden. Error: %s",
                    file.getOriginalFilename(), e.getMessage());
            logger.severe(message);
            // Todo throw custom Exception
        }
    }

    /**
     * Get all class lists from the database.
     * @return a list of {@link StudentDTO}.
     */
    public List<StudentDTO> getAllClassLists() {
        Function<Student, StudentDTO> mapStudentToDto = student -> StudentDTO.builder()
                .email(student.getEmail())
                .name(student.getName())
                .clazz(student.getClazz())
                .paDispensation(PA_DISPENSATION)
                .wpmDispensation(WPM_DISPENSATION)
                .isIP(student.isIP())
                .build();

        return classListRepository
                .findAll()
                .stream()
                .map(mapStudentToDto)
                .toList();
    }

}
