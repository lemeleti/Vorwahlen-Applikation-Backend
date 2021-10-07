package ch.zhaw.vorwahlen.controller;

import ch.zhaw.vorwahlen.model.dto.ModuleDTO;
import ch.zhaw.vorwahlen.model.service.ModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controller for a module.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("module")
public class ModuleController {
    private final ModuleService moduleService;

    /**
     * Return all modules.
     * @return {@link ResponseEntity<List<ModuleDTO>>}
     */
    @GetMapping(path = {"/", ""})
    public ResponseEntity<List<ModuleDTO>> getAllModules() {
        return ResponseEntity.ok().body(moduleService.getAllModules());
    }

    /**
     * Import module list from excel.
     * @param file the excel file
     * @return {@link ResponseEntity<String>}
     */
    @PostMapping(path = {"/", ""})
    public ResponseEntity<String> saveModulesFromExcel(@RequestParam("file") MultipartFile file) {
        moduleService.importModuleExcel(file);
        return ResponseEntity.ok().build();
    }

    /**
     * Scrape external website to retrieve additional data from the module list.
     * @return {@link ResponseEntity<String>}
     */
    @PostMapping(path = "/scrape")
    public ResponseEntity<String> fetchAdditionalModuleData() {
        moduleService.fetchAdditionalModuleData();
        return ResponseEntity.ok().build();
    }
}
