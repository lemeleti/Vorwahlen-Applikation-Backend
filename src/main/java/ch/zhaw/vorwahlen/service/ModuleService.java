package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.config.ResourceBundleMessageLoader;
import ch.zhaw.vorwahlen.constants.ResourceMessageConstants;
import ch.zhaw.vorwahlen.exception.ImportException;
import ch.zhaw.vorwahlen.exception.ModuleNotFoundException;
import ch.zhaw.vorwahlen.mapper.Mapper;
import ch.zhaw.vorwahlen.model.dto.EventoDataDTO;
import ch.zhaw.vorwahlen.model.dto.ModuleDTO;
import ch.zhaw.vorwahlen.model.modules.EventoData;
import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.Student;
import ch.zhaw.vorwahlen.parser.ModuleParser;
import ch.zhaw.vorwahlen.repository.EventoDataRepository;
import ch.zhaw.vorwahlen.repository.ModuleRepository;
import ch.zhaw.vorwahlen.scraper.EventoScraper;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Business logic for the modules.
 */
@RequiredArgsConstructor
@Service
@Log
public class ModuleService {

    private static final int MAX_THREAD_NUMBER = 10;

    private final ModuleRepository moduleRepository;
    private final EventoDataRepository eventoDataRepository;
    private final Mapper<ModuleDTO, Module> moduleMapper;
    private final Mapper<EventoDataDTO, EventoData> eventoDataMapper;

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
            var formatString = ResourceBundleMessageLoader.getMessage(ResourceMessageConstants.ERROR_IMPORT_EXCEPTION);
            var message = String.format(formatString, file.getOriginalFilename());
            throw new ImportException(message, e);
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
        var levenshteinDistance = LevenshteinDistance.getDefaultInstance()
                .apply(m1.getShortModuleNo(), m2.getShortModuleNo());
        var isValid = levenshteinDistance == 1;

        var difference1 = StringUtils.difference(m1.getShortModuleNo(), m2.getShortModuleNo());
        var difference2 = StringUtils.difference(m2.getShortModuleNo(), m1.getShortModuleNo());

        if(isValid) {
            var isDiff1FirstCharNumeric = StringUtils.isNumeric(String.valueOf(difference1.charAt(0)));
            var isDiff2FirstCharNumeric = StringUtils.isNumeric(String.valueOf(difference2.charAt(0)));

            if (difference1.length() != difference2.length()) {
                isValid = difference1.length() > difference2.length()
                        ? isDiff1FirstCharNumeric
                        : isDiff2FirstCharNumeric;
            } else {
                isValid = isDiff1FirstCharNumeric && isDiff2FirstCharNumeric;
            }
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
    public List<ModuleDTO> getAllModules(Student student) {
        List<Module> modules;

        if (student != null && student.isTZ() && student.isSecondElection()) {
            modules = moduleRepository.findAllModulesTZSecondHalf();
        } else if (student != null && student.isTZ()) {
            modules = moduleRepository.findAllModulesTZFirstHalf();
        } else {
            modules = moduleRepository.findAll();
        }

        return modules.stream().map(moduleMapper::toDto).toList();
    }

    private Module addModule(ModuleDTO moduleDTO) {
        Function<List<Integer>, String> parseExecutionSemesterList = list ->
                list.stream()
                        .sorted()
                        .map(String::valueOf)
                        .collect(Collectors.joining(";"));
        var executionSemester = moduleDTO.getExecutionSemester();
        var module = Module.builder()
                .moduleNo(moduleDTO.getModuleNo())
                .shortModuleNo(moduleDTO.getShortModuleNo())
                .moduleTitle(moduleDTO.getModuleTitle())
                .moduleId(moduleDTO.getModuleId())
                .moduleGroup(moduleDTO.getModuleGroup())
                .isIPModule(moduleDTO.isIPModule())
                .institute(moduleDTO.getInstitute())
                .credits(moduleDTO.getCredits())
                .language(moduleDTO.getLanguage())
                .fullTimeSemester(parseExecutionSemesterList.apply(executionSemester.fullTimeSemesterList()))
                .partTimeSemester(parseExecutionSemesterList.apply(executionSemester.partTimeSemesterList()))
                .consecutiveModuleNo(moduleDTO.getConsecutiveModuleNo())
                .build();
        return moduleRepository.save(module);
    }

    public URI addAndReturnLocation(ModuleDTO moduleDTO) {
        var addedModule = addModule(moduleDTO);
        try {
            return new URI("/module/".concat(addedModule.getModuleNo()));
        } catch (URISyntaxException e) {
            throw new RuntimeException();
        }
    }

    /**
     * Get module from the database by id.
     * @return {@link ModuleDTO}.
     */
    public ModuleDTO getModuleById(String id) {
        return moduleRepository
                .findById(id)
                .map(moduleMapper::toDto)
                .orElseThrow(() -> {
                    var formatString = ResourceBundleMessageLoader.getMessage(ResourceMessageConstants.ERROR_MODULE_NOT_FOUND);
                    var errorMessage = String.format(formatString, id);
                    return new ModuleNotFoundException(errorMessage);
                });
    }

    public void deleteModuleById(String id) {
        moduleRepository.deleteById(id);
    }

    public ModuleDTO replaceModule(String id, ModuleDTO moduleDTO) {
        var updatedModule = moduleRepository.findById(id)
                .map(module -> {
                    module.setModuleNo(moduleDTO.getModuleNo());
                    module.setModuleTitle(moduleDTO.getModuleTitle());
                    module.setCredits(moduleDTO.getCredits());
                    module.setLanguage(moduleDTO.getLanguage());
                    // todo: module.executionSemester(toExecutionSemester(moduleDTO));
                    module.setConsecutiveModuleNo(moduleDTO.getConsecutiveModuleNo());
                    return moduleRepository.save(module);
                })
                .orElse(addModule(moduleDTO));

        return moduleMapper.toDto(updatedModule);
    }

    /**
     * Get additional information by the module id
     * @param id module id
     * @return additional data as {@link EventoDataDTO}
     */
    public EventoDataDTO getEventoDataById(String id) {
        var eventoData = eventoDataRepository.getById(id);
        return eventoDataMapper.toDto(eventoData);
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
