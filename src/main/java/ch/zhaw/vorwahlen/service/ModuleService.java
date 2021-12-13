package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.config.ResourceBundleMessageLoader;
import ch.zhaw.vorwahlen.constants.ResourceMessageConstants;
import ch.zhaw.vorwahlen.exception.EventoDataNotFoundException;
import ch.zhaw.vorwahlen.exception.ImportException;
import ch.zhaw.vorwahlen.exception.ModuleConflictException;
import ch.zhaw.vorwahlen.exception.ModuleNotFoundException;
import ch.zhaw.vorwahlen.mapper.Mapper;
import ch.zhaw.vorwahlen.model.dto.EventoDataDTO;
import ch.zhaw.vorwahlen.model.dto.ModuleDTO;
import ch.zhaw.vorwahlen.model.modules.EventoData;
import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.parser.ModuleParser;
import ch.zhaw.vorwahlen.repository.ElectionRepository;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import static ch.zhaw.vorwahlen.constants.ResourceMessageConstants.ERROR_EVENTO_MODULE_NOT_FOUND;

/**
 * Business logic for the modules.
 */
@RequiredArgsConstructor
@Service
@Log
public class ModuleService {
    private final ModuleRepository moduleRepository;
    private final EventoDataRepository eventoDataRepository;
    private final ElectionRepository electionRepository;
    private final Mapper<ModuleDTO, Module> moduleMapper;
    private final Mapper<EventoDataDTO, EventoData> eventoDataMapper;

    /**
     * Importing the Excel file and storing the needed content into the database.
     * @param file the Excel file to be parsed and stored.
     */
    public void importModuleExcel(MultipartFile file, String worksheet) {
        try {
            var moduleParser = new ModuleParser(file.getInputStream(), worksheet);
            var modules = moduleParser.parseFromXLSX();
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

    /**
     * Check if the module short numbers differ by only one numeric value.
     * @param m1 the first {@link Module} to be compared
     * @param m2 the other {@link Module} to be compared
     * @return true or false
     */
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
    public List<ModuleDTO> getAllModules() {
        return moduleRepository.findAll().stream().map(moduleMapper::toDto).toList();
    }

    /**
     * Add a new module.
     * @param moduleDTO to be added module.
     * @return path where the module can be fetched.
     */
    public ModuleDTO addModule(ModuleDTO moduleDTO) {
        if(moduleRepository.existsById(moduleDTO.getModuleNo())) {
            var formatString = ResourceBundleMessageLoader.getMessage(ResourceMessageConstants.ERROR_MODULE_CONFLICT);
            var message = String.format(formatString, moduleDTO.getModuleNo());
            throw new ModuleConflictException(message);
        }
        var module = moduleMapper.toInstance(moduleDTO);
        module = moduleRepository.save(module);
        return moduleMapper.toDto(module);
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

    /**
     * Delete module by id.
     * @param id to be deleted module.
     */
    public void deleteModuleById(String id) {
        var module = fetchModuleById(id);
        var elections = electionRepository.findAllByElectedModulesContaining(module);

        elections.forEach(moduleElection -> {
            moduleElection.removeModuleFromElection(module);
            moduleElection.setElectionValid(false);
        });
        moduleRepository.deleteById(id);
        if(eventoDataRepository.existsById(id)) {
            eventoDataRepository.deleteById(id);
        }
    }

    /**
     * Replace module by id.
     * @param id to be replaced module
     * @param moduleDTO new module
     * @return saved module
     */
    public ModuleDTO replaceModule(String id, ModuleDTO moduleDTO) {
        var storedModule = fetchModuleById(id);
        var newModule = moduleMapper.toInstance(moduleDTO);
        newModule.setModuleNo(storedModule.getModuleNo());
        return moduleMapper.toDto(moduleRepository.save(newModule));
    }

    /**
     * Get additional information by the module id
     * @param id module id
     * @return additional data as {@link EventoDataDTO}
     */
    public EventoDataDTO getEventoDataById(String id) {
        var eventoData = eventoDataRepository.findById(id).orElseThrow(() -> {
            var errorMessage = String.format(ResourceBundleMessageLoader.getMessage(ERROR_EVENTO_MODULE_NOT_FOUND), id);
            return new EventoDataNotFoundException(errorMessage);
        });
        return eventoDataMapper.toDto(eventoData);
    }

    /**
     * Runs the scraper for all modules to retrieve additional data.
     */
    @Async
    public void scrapeEventoDataForAllModules() {
        var eventoDataList = new ArrayList<EventoData>();
        var modules = moduleRepository.findAll();
        for (var module : modules) {
            var eventoUrl = String.format(EventoScraper.SITE_URL, module.getModuleId());
            var data = EventoScraper.parseModuleByURL(eventoUrl, module);
            eventoDataList.add(data);
            log.info(data.toString());
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                log.log(Level.WARNING, e.getLocalizedMessage(), e);
            }
        }
        eventoDataRepository.saveAll(eventoDataList);
    }

    public EventoDataDTO scrapeEventoDataForId(String id) {
        var module = fetchModuleById(id);
        var eventoUrl = String.format(EventoScraper.SITE_URL, module.getModuleId());
        return eventoDataMapper.toDto(eventoDataRepository.save(EventoScraper.parseModuleByURL(eventoUrl, module)));
    }

    private Module fetchModuleById(String id) {
        return moduleRepository
                .findById(id)
                .orElseThrow(() -> {
                    var formatString = ResourceBundleMessageLoader.getMessage(ResourceMessageConstants.ERROR_MODULE_NOT_FOUND);
                    var errorMessage = String.format(formatString, id);
                    return new ModuleNotFoundException(errorMessage);
                });
    }
}