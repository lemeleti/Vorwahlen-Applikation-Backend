package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.model.dto.ModuleDTO;
import ch.zhaw.vorwahlen.model.modules.EventoData;
import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.parser.ModuleParser;
import ch.zhaw.vorwahlen.repository.EventoDataRepository;
import ch.zhaw.vorwahlen.repository.ModuleRepository;
import ch.zhaw.vorwahlen.scraper.EventoScraper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Business logic for the modules.
 */
@RequiredArgsConstructor
@Service
public class ModuleService {

    private static final int MAX_THREAD_NUMBER = 10;

    private final Logger logger = Logger.getLogger(ModuleService.class.getName());

    private final ModuleRepository moduleRepository;
    private final EventoDataRepository eventoDataRepository;

    /**
     * Importing the Excel file and storing the needed content into the database.
     * @param file the Excel file to be parsed and stored.
     */
    public void importModuleExcel(MultipartFile file, String worksheet) {
        try {
            var moduleParser = new ModuleParser(file.getInputStream(), worksheet);
            var modules = moduleParser.parseModulesFromXLSX();
            moduleRepository.saveAll(modules);
        } catch (IOException e) {
            var message = String.format("Die Datei %s konnte nicht abgespeichert werden. Error: %s",
                    file.getOriginalFilename(), e.getMessage());
            logger.severe(message);
            // Todo throw custom Exception
        }
    }

    /**
     * Get all modules from the database.
     * @return a list of {@link ModuleDTO}.
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
        var executorService = Executors.newFixedThreadPool(MAX_THREAD_NUMBER);
        var startedThreads = startThreads(executorService);
        saveFetchedModuleData(startedThreads);
        executorService.shutdown();
    }

    private void saveFetchedModuleData(List<Future<EventoData>> futures) {
        for (var future : futures) {
            try {
                eventoDataRepository.save(future.get());
            } catch (InterruptedException | ExecutionException e) {
                // not re-interrupting because this is not critical and is logged
                logger.severe(e.getMessage());
            }
        }
    }

    private List<Future<EventoData>> startThreads(ExecutorService executorService) {
        Function<Integer, Future<EventoData>> startThread = moduleId -> executorService.submit(() -> {
            var eventoUrl = String.format(EventoScraper.SITE_URL, moduleId);
            return EventoScraper.parseModuleByURL(eventoUrl);
        });

        return moduleRepository.findAll()
                .stream()
                .map(Module::getModuleId)
                .map(startThread)
                .toList();
    }

}
