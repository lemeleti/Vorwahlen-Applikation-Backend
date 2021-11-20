package ch.zhaw.vorwahlen.controller;

import ch.zhaw.vorwahlen.model.dto.EventoDataDTO;
import ch.zhaw.vorwahlen.model.dto.ModuleDTO;
import ch.zhaw.vorwahlen.service.ModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
     * @return {@link ResponseEntity<List<ModuleDTO>>} with status code ok
     */
    @GetMapping(path = {"/", ""})
    public ResponseEntity<List<ModuleDTO>> getAllModules() {
        return ResponseEntity.ok().body(moduleService.getAllModules());
    }

    /**
     * Import module list from Excel.
     * @param file the Excel file.
     * @return {@link ResponseEntity<String>} with status code ok or bad request if the provided file is not there
     */
    @PostMapping(path = {"/", ""})
    public ResponseEntity<String> saveModulesFromExcel(@RequestParam("file") MultipartFile file,
                                                       @RequestParam("worksheet") String worksheet) {
        if (file.isEmpty()) return ResponseEntity.badRequest().build();
        moduleService.importModuleExcel(file, worksheet);
        return ResponseEntity.ok().build();
    }

    /**
     * Returns the additional data of a module.
     * @return {@link ResponseEntity<EventoDataDTO>} with status code ok
     */
    @GetMapping(path = {"/eventodata/{moduleKuerzel}"})
    public ResponseEntity<EventoDataDTO> getAdditionalModuleDataByKuerzel(@PathVariable String moduleKuerzel) {
        return ResponseEntity.ok().body(moduleService.getEventoDataById(moduleKuerzel));
    }

    /**
     * Scrape external website to retrieve additional data from the module list.
     * @return {@link ResponseEntity<String>} with status code ok
     */
    @PostMapping(path = "/scrape")
    public ResponseEntity<String> fetchAdditionalModuleData() {
        moduleService.fetchAdditionalModuleData();
        return ResponseEntity.ok().build();
    }

}
