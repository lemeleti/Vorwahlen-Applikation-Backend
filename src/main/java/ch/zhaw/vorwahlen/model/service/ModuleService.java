package ch.zhaw.vorwahlen.model.service;

import ch.zhaw.vorwahlen.model.dto.ModuleDTO;
import ch.zhaw.vorwahlen.model.modules.EventoData;
import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.parser.ModuleParser;
import ch.zhaw.vorwahlen.repository.EventoDataRepository;
import ch.zhaw.vorwahlen.repository.ModuleRepository;
import ch.zhaw.vorwahlen.scraper.EventoScraper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;

/**
 * Business logic for the modules
 */
@RequiredArgsConstructor
@Service
public class ModuleService {
    private final Logger logger = Logger.getLogger(ModuleService.class.getName());

    private final ModuleRepository moduleRepository;
    private final EventoDataRepository eventoDataRepository;
    private final Environment env;

    /**
     * Saves modules based on an excel file.
     * @param file the excel file to be parsed
     */
    public void saveModulesFromExcel(MultipartFile file) {
        try {
            String location = saveFileToDisk(file);
            ModuleParser moduleParser = new ModuleParser(location, "Module 2025");
            List<Module> modules = moduleParser.parseModulesFromXLSX();
            moduleRepository.saveAll(modules);
        } catch (IOException e) {
            String message = String.format("Die Datei %s konnte nicht abgespeichert werden. Error: %s",
                    file.getOriginalFilename(), e.getMessage());
            logger.severe(message);
            // Todo throw custom Exception
        }
    }

    /**
     * Get all modules from the database
     * @return a list of {@link Module}
     */
    public List<ModuleDTO> getAllModules() {
        return moduleRepository
                .findAll()
                .stream()
                .map(module -> new ModuleDTO(module.getModuleNo(), module.getModuleTitle(), module.getLanguage()))
                .toList();
    }

    /**
     * Runs the scraper for all modules to retrieve additional data.
     */
    @Async
    public void fetchAdditionalModuleData() {
        ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        List<Future<EventoData>> futures = new LinkedList<>();
        moduleRepository.findAll()
                .stream()
                .map(Module::getModuleId)
                .forEach(moduleId -> futures.add(
                    executorService.submit(() -> {
                        String eventoUrl = String.format(EventoScraper.SITE_URL, moduleId);
                        return EventoScraper.parseModuleByURL(eventoUrl);
                    })
                ));

        for (var future : futures) {
            try {
                eventoDataRepository.save(future.get());
            } catch (InterruptedException | ExecutionException e) {
                logger.severe(e.getMessage());
            }
        }
    }

    private String saveFileToDisk(MultipartFile file) throws IOException {
        String filePath = env.getProperty("upload.dir") + File.separator + file.getOriginalFilename();
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            for (byte b : file.getInputStream().readAllBytes()) {
                fos.write(b);
            }
        }
        return filePath;
    }
}
