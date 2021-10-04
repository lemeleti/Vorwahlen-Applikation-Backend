package ch.zhaw.vorwahlen.model.service;

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

@RequiredArgsConstructor
@Service
public class ModuleService {
    private final Logger logger = Logger.getLogger(ModuleService.class.getName());

    private final ModuleRepository moduleRepository;
    private final EventoDataRepository eventoDataRepository;
    private final Environment env;

    public void createModuleByXLSX(MultipartFile file) {
        try {
            String location = saveFileToDisk(file);
            List<Module> modules = ModuleParser.parseModulesFromXLSX(location);
            moduleRepository.saveAll(modules);
        } catch (IOException e) {
            String message = String.format("Die Datei %s konnte nicht abgespeichert werden. Error: %s",
                    file.getOriginalFilename(), e.getMessage());
            logger.severe(message);
            // Todo throw custom Exception
        }
    }

    public List<Module> getAllModules() {
        return moduleRepository.findAll();
    }

    @Async
    public void fetchAdditionalModuleData() {
        ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        List<Future<EventoData>> futures = new LinkedList<>();
        getAllModules()
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
