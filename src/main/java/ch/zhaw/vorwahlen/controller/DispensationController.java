package ch.zhaw.vorwahlen.controller;

import ch.zhaw.vorwahlen.service.DispensationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller for the class lists.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("dispensation")
public class DispensationController {
    private final DispensationService dispensationService;

    /**
     * Import dispensation list from Excel.
     * @param file the Excel file.
     * @return {@link ResponseEntity<String>}
     */
    @PostMapping(path = {"/", ""})
    public ResponseEntity<String> saveDispensationListFromExcel(@RequestParam("file") MultipartFile file,
                                                       @RequestParam("worksheet") String worksheet) {
        if (file.isEmpty()) return ResponseEntity.notFound().build();
        dispensationService.importDispensationExcel(file, worksheet);
        return ResponseEntity.ok().build();
    }
}
