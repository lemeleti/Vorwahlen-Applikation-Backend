package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.config.ResourceBundleMessageLoader;
import ch.zhaw.vorwahlen.constants.ResourceMessageConstants;
import ch.zhaw.vorwahlen.exception.ImportException;
import ch.zhaw.vorwahlen.parser.DispensationParser;
import ch.zhaw.vorwahlen.repository.ClassListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;

/**
 * Business logic for the modules.
 */
@RequiredArgsConstructor
@Service
@Log
public class DispensationService {

    private final ClassListRepository classListRepository;

    /**
     * Importing the Excel file and storing the needed content into the database.
     * @param file the Excel file to be parsed and stored.
     */
    public void importDispensationExcel(MultipartFile file, String worksheet) {
        try {
            var dispensationParser = new DispensationParser(file.getInputStream(), worksheet);
            var parsedList = dispensationParser.parseModulesFromXLSX();
            parsedList.forEach(student -> {
                try {
                    var dbStudent = classListRepository.getById(student.getEmail());
                    dbStudent.setPaDispensation(student.getPaDispensation());
                    dbStudent.setWpmDispensation(student.getWpmDispensation());
                    classListRepository.save(dbStudent);
                } catch (EntityNotFoundException e) {
                    log.warning(e.getMessage());
                }
            });
        } catch (IOException e) {
            var formatString = ResourceBundleMessageLoader.getMessage(ResourceMessageConstants.ERROR_IMPORT_EXCEPTION);
            var message = String.format(formatString, file.getOriginalFilename());
            throw new ImportException(message, e);
        }
    }

}
