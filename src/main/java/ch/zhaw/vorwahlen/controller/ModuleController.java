package ch.zhaw.vorwahlen.controller;

import ch.zhaw.vorwahlen.model.dto.ModuleDTO;
import ch.zhaw.vorwahlen.model.service.ModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("module")
public class ModuleController {
    private final ModuleService moduleService;

    @GetMapping(path = {"/", ""})
    public ResponseEntity<List<ModuleDTO>> index() {
        return ResponseEntity.ok().body(moduleService.getAllModules());
    }

    @PostMapping(path = {"/", ""})
    public ResponseEntity<String> createModuleByXLSX(@RequestParam("file") MultipartFile file) {
        moduleService.createModuleByXLSX(file);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/scrape")
    public ResponseEntity<String> fetchAdditionalModuleData() {
        moduleService.fetchAdditionalModuleData();
        return ResponseEntity.ok().build();
    }
}
