package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.model.dto.EventoDataDTO;
import ch.zhaw.vorwahlen.model.dto.ModuleDTO;
import ch.zhaw.vorwahlen.model.modules.EventoData;
import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.parser.ModuleParser;
import ch.zhaw.vorwahlen.repository.EventoDataRepository;
import ch.zhaw.vorwahlen.repository.ModuleRepository;
import ch.zhaw.vorwahlen.scraper.EventoScraper;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * Business logic for the modules.
 */
@RequiredArgsConstructor
@Service
@Log
public class ModuleService {

    private static final int MAX_THREAD_NUMBER = 10;
    private static final int DIFF_INDEX_OFFSET = 1;

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
            var updatedModules = setConsecutiveModules(modules);
            moduleRepository.saveAll(updatedModules);
        } catch (IOException e) {
            var message = String.format("Die Datei %s konnte nicht abgespeichert werden. Error: %s",
                    file.getOriginalFilename(), e.getMessage());
            log.severe(message);
            // Todo throw custom Exception
        }
    }

    private Set<Module> setConsecutiveModules(List<Module> modules) {
        var consecutiveSet = new HashSet<Module>();
        for (var m1: modules) {
            for(var m2: modules) {
                if(!m1.equals(m2)
                        && isModuleConsecutiveFieldNotSet(m1)
                        && isModuleConsecutiveFieldNotSet(m2)
                        && doTheModulesDifferOnlyInTheNumber(m1, m2)) {
                    consecutiveSet.add(m1);
                    consecutiveSet.add(m2);
                    m1.setConsecutiveModuleNo(m2.getModuleNo());
                    m2.setConsecutiveModuleNo(m1.getModuleNo());
                }
            }
        }
        return consecutiveSet;
    }

    public static boolean doTheModulesDifferOnlyInTheNumber(Module m1, Module m2) {
        // t.BA.WV.AI1-EN.19HS, t.BA.WV.AI2-EN.20HS -> 2-EN.20HS
        var difference1 = StringUtils.difference(m1.getModuleNo(), m2.getModuleNo());

        // 2-EN.20HS -> 2-EN
        var semesterYearPartIndex = difference1.lastIndexOf(".");
        var restDifference1 = difference1.substring(0, semesterYearPartIndex);

        // t.BA.WV.AI2-EN.20HS, t.BA.WV.AI1-EN.19HS -> 1-EN.19HS
        var difference2 = StringUtils.difference(m2.getModuleNo(), m1.getModuleNo());

        // 1-EN.19HS -> 1-EN
        semesterYearPartIndex = difference2.lastIndexOf(".");
        var restDifference2 = difference2.substring(0, semesterYearPartIndex);

        var absoluteDifference = Math.abs(restDifference1.length() - restDifference2.length());

        var isValid = false;
        var isDiff1FirstCharNumeric = StringUtils.isNumeric(String.valueOf(difference1.charAt(0)));
        var isDiff2FirstCharNumeric = StringUtils.isNumeric(String.valueOf(difference2.charAt(0)));
        if(absoluteDifference == 0) {
            // example case t.BA.WV.AI1-EN.19HS, t.BA.WV.AI2-EN.20HS
            isValid = isDiff1FirstCharNumeric && isDiff2FirstCharNumeric;
        } else if (absoluteDifference == 1) {
            // example case t.BA.WV.DIP-EN.19HS, t.BA.WV.DIP2-EN.20HS
            isValid = difference1.length() > difference2.length() // diff1 = 2-EN, diff2 = -EN
                    ? isDiff1FirstCharNumeric
                    : isDiff2FirstCharNumeric;
        }
        return isValid;
    }

    private boolean isModuleConsecutiveFieldNotSet(Module module) {
        return module.getConsecutiveModuleNo() == null || module.getConsecutiveModuleNo().isBlank();
    }

    /**
     * Get all modules from the database.
     * @return a list of {@link ModuleDTO}.
     */
    public List<ModuleDTO> getAllModules() {
        return moduleRepository
                .findAll()
                .stream()
                .map(DTOMapper.mapModuleToDto)
                .toList();
    }

    /**
     * Get module from the database by id.
     * @return {@link ModuleDTO}.
     */
    public Optional<ModuleDTO> getModuleById(String id) {
        return moduleRepository
                .findById(id)
                .stream()
                .map(DTOMapper.mapModuleToDto)
                .findFirst();
    }

    /**
     * Get additional information by the module id
     * @param id module id
     * @return additional data as {@link EventoDataDTO}
     */
    public EventoDataDTO getEventoDataById(String id) {
        var eventoData = eventoDataRepository.getById(id);
        return DTOMapper.mapEventoDataToDto.apply(eventoData);
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
                log.severe(e.getMessage());
            }
        }
    }

    private List<Future<EventoData>> startThreads(ExecutorService executorService) {
        Function<Module, Future<EventoData>> startThread = module -> executorService.submit(() -> {
            var eventoUrl = String.format(EventoScraper.SITE_URL, module.getModuleId());
            return EventoScraper.parseModuleByURL(eventoUrl, module);
        });

        return moduleRepository.findAll()
                .stream()
                .map(startThread)
                .toList();
    }

}
