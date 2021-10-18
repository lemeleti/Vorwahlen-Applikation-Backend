package ch.zhaw.vorwahlen.controller;

import ch.zhaw.vorwahlen.model.dto.StudentDTO;
import ch.zhaw.vorwahlen.service.ClassListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controller for the class lists.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("class")
public class ClassListController {
    private final ClassListService classListService;

    /**
     * Return all class lists.
     * @return {@link ResponseEntity<List<StudentDTO>>}
     */
    @GetMapping(path = {"/", ""})
    public ResponseEntity<List<StudentDTO>> getAllClassLists() {
        return ResponseEntity.ok().body(classListService.getAllClassLists());
    }

    /**
     * Import module list from Excel.
     * @param file the Excel file.
     * @return {@link ResponseEntity<String>}
     */
    @PostMapping(path = {"/", ""})
    public ResponseEntity<String> saveClassListsFromExcel(@RequestParam("file") MultipartFile file,
                                                       @RequestParam("worksheet") String worksheet) {
        if (file.isEmpty()) return ResponseEntity.notFound().build();
        classListService.importClassListExcel(file, worksheet);
        return ResponseEntity.ok().build();
    }
}
