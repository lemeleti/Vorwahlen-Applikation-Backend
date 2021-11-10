package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.model.dto.ModuleElectionDTO;
import ch.zhaw.vorwahlen.model.dto.StudentDTO;
import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleCategory;
import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import ch.zhaw.vorwahlen.repository.ElectionRepository;
import ch.zhaw.vorwahlen.repository.ModuleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Business logic for the election.
 */
@RequiredArgsConstructor
@Service
@Log
public class ElectionService {

    public static final int MAX_CREDITS_PER_YEAR_WITHOUT_PA_AND_BA = 42; // PA = 6 Credits, BA = 12 Credits
    public static final int NUM_CONTEXT_MODULES = 3;
    public static final int NUM_SUBJECT_MODULES = 8;
    public static final int NUM_INTERDISCIPLINARY_MODULES = 1;
    public static final int NUM_ENGLISH_CREDITS = 20;
    public static final int CREDIT_PER_SUBJECT_MODULE = 4;

    private final ElectionRepository electionRepository;
    private final Function<Set<String>, Set<Module>> mapModuleSet;

    @Autowired
    public ElectionService(ElectionRepository electionRepository, ModuleRepository moduleRepository) {
        this.electionRepository = electionRepository;
        this.mapModuleSet = list -> list.stream()
                                        .map(moduleRepository::getById)
                                        .collect(Collectors.toSet());
    }

    /**
     * Gets the stored election from student.
     * @param studentDTO student in session
     * @return current election
     */
    public ModuleElectionDTO getModuleElectionByStudent(StudentDTO studentDTO) {
        var optional = electionRepository.findById(studentDTO.getEmail());
        if(optional.isPresent()) {
            return optional.map(DTOMapper.mapElectionToDto).get();
        }
        return null;
    }

    /**
     * Saves the election to the database.
     * @param studentDTO student in session
     * @param moduleElectionDTO his current election
     * @return true - if save successful<br>
     *         false - if arguments invalid
     */
    public ObjectNode saveElection(StudentDTO studentDTO, ModuleElectionDTO moduleElectionDTO) {
        // optional todo: test double modules like MC1/MC2 (not one missing)
        if(studentDTO == null || moduleElectionDTO == null
                || studentDTO.getEmail() == null || studentDTO.getEmail().isBlank()) {
            return createSaveStatusBundle(false, false);
        }
        var moduleElection = DTOMapper.mapDtoToModuleElection(moduleElectionDTO, studentDTO, mapModuleSet);

        var isValid = validateElection(studentDTO, moduleElection);
        moduleElection.setStudentEmail(studentDTO.getEmail());
        moduleElection.setElectionValid(isValid);
        moduleElectionDTO.setElectionValid(isValid); // needed in unit tests
        electionRepository.save(moduleElection);

        return createSaveStatusBundle(true, isValid);
    }

    private ObjectNode createSaveStatusBundle(boolean saveSuccess, boolean validElection) {
        var mapper = new ObjectMapper();
        var node = mapper.createObjectNode();
        node.put("election_saved", saveSuccess);
        node.put("election_valid", validElection);
        return node;
    }

    /**
     * Validates the election.
     * @param studentDTO student in session
     * @param moduleElection his current selection
     * @return true - if election is valid<br>
     *         false - if election is invalid
     */
    public boolean validateElection(StudentDTO studentDTO, ModuleElection moduleElection) {
        var isValid = false;
        if(studentDTO.isTZ()) {
            // NOTE: IP not checked because we don't store the elected modules from the previous year.
            // todo: validation for 5/6 semester or 7/8 semester
            /*
             * Konsekutive Wahlpfichtmodule: (Like MC1/MC2)
             * IT18 Teilzeit: Wenn Sie im aktuellen Studienjahr schon zwei konsekutive Module belegt haben, wählen Sie mindestens einmal zwei konsekutive Module, ansonsten mindestens zweimal zwei konsekutive Module.
             * IT19 Teilzeit: Wählen Sie bis zu zwei konsekutive Module (empfohlen: zwei Module). Achten Sie speziell auf die nötigen Vorkenntnisse der Module.
             *
             * Wahlpfichtmodule: (Like AI)
             * IT18 Teilzeit: Zusammen mit den oben gewählten konsekutiven Modulen, wählen Sie total sieben Module (mit genehmigter Dispensation aus der beruflichen Anrechnung fünf Module)
             * IT19 Teilzeit: Zusammen mit den oben gewählten konsekutiven Modulen wählen Sie total zwei Module. In der Regel wählen Sie hier also kein Modul.
             *
             * Wahlmodule: (like PHMOD)
             * IT18 Teilzeit: Sie können bis zu einem der oben gewählten Wahlpflichtmodule durch ein Wahlmodule ersetzen.
             * IT19 Teilzeit: In der Regel wählen Sie jetzt noch keines dieser Wahlmodule, da es mit grosser Wahrscheinlichkeit mit Ihrem obligatorischen Stundenplan nicht kompatibel ist.
             *
             * Wirtschaft und Recht: (like RM)
             * IT18 Teilzeit: Dies gehört zur Modulgruppe IT4. Sie können bis zu zwei dieser Module wählen. Das dritte Modul ist ein METU-Modul, das können Sie hier noch nicht vorwählen.
             * IT19 Teilzeit: Dies gehört zur Modulgruppe IT5. Sie können bis zu zwei dieser Module für das kommende Studienjahr wählen.
             */
        } else {
            isValid =  isOverflownEmpty(moduleElection)
                    && isCreditSumValid(moduleElection, studentDTO)
                    && validContextModuleElection(moduleElection)
                    && validSubjectModuleElection(moduleElection, studentDTO)
                    && validInterdisciplinaryModuleElection(moduleElection)
                    && validIpModuleElection(moduleElection, studentDTO)
                    && validConsecutiveModuleElection(moduleElection);
        }
        return isValid;
    }

    private boolean validConsecutiveModuleElection(ModuleElection moduleElection) {
        var consecutiveMap = new HashMap<Module, Module>();
        for(var m1: moduleElection.getElectedModules()) {
            for(var m2: moduleElection.getElectedModules()) {
                if(!m1.equals(m2) && areModulesConsecutive(m1, m2)) {
                    consecutiveMap.putIfAbsent(m1, null);
                    if (ModuleService.doTheModulesDifferOnlyInTheNumber(m1, m2)){
                        consecutiveMap.put(m1, m2);
                    }
                }
            }
        }

        var countConsecutiveMissingPart = consecutiveMap.values().stream()
                .filter(Objects::isNull)
                .count();

        return consecutiveMap.size() != 0 && countConsecutiveMissingPart == 0 &&
                hasAtLeastTwoConsecutiveModules(moduleElection, consecutiveMap);
    }

    private boolean hasAtLeastTwoConsecutiveModules(ModuleElection moduleElection, Map<Module, Module> consecutiveMap) {
        return consecutiveMap.size() > 2 ||
                (consecutiveMap.size() == 1
                    && containsModule(moduleElection.getElectedModules(), "t.BA.WV.PSPP.19HS")
                    && containsModule(moduleElection.getElectedModules(), "t.BA.WV.FUP.19HS"));
    }

    private boolean areModulesConsecutive(Module m1, Module m2) {
        return m1.getConsecutiveModuleNo() != null && !m1.getConsecutiveModuleNo().isBlank()
                && m2.getConsecutiveModuleNo() != null && !m2.getConsecutiveModuleNo().isBlank();
    }

    private boolean containsModule(Set<Module> modules, String moduleNo) {
        return modules.stream()
                .filter(module -> moduleNo.equals(module.getModuleNo()))
                .count() == 1;
    }

    private boolean validIpModuleElection(ModuleElection moduleElection, StudentDTO studentDTO) {
        var isValid = true;
        if(studentDTO.isIP()) {
            var sum = moduleElection.getElectedModules().stream()
                    .filter(module -> "Englisch".equals(module.getLanguage()))
                    .mapToInt(Module::getCredits)
                    .sum();
            // todo: ask for dispensations in ip
            isValid = sum + studentDTO.getWpmDispensation() >= NUM_ENGLISH_CREDITS;
        }
        return isValid;
    }

    private long countModuleCategory(ModuleElection moduleElection, ModuleCategory moduleCategory) {
        return moduleElection.getElectedModules()
                .stream()
                .map(module -> ModuleCategory.parse(module.getModuleNo(), module.getModuleGroup()))
                .filter(category -> category == moduleCategory)
                .count();
    }

    private boolean validInterdisciplinaryModuleElection(ModuleElection moduleElection) {
        var count = countModuleCategory(moduleElection, ModuleCategory.INTERDISCIPLINARY_MODULE);
        return count == NUM_INTERDISCIPLINARY_MODULES;
    }

    private boolean validSubjectModuleElection(ModuleElection moduleElection, StudentDTO studentDTO) {
        var count = countModuleCategory(moduleElection, ModuleCategory.SUBJECT_MODULE);
        var dispensCount = studentDTO.getWpmDispensation() / CREDIT_PER_SUBJECT_MODULE;
        return count + dispensCount == NUM_SUBJECT_MODULES;
    }

    private boolean validContextModuleElection(ModuleElection moduleElection) {
        var count = countModuleCategory(moduleElection, ModuleCategory.CONTEXT_MODULE);
        return count == NUM_CONTEXT_MODULES;
    }

    private boolean isCreditSumValid(ModuleElection moduleElection, StudentDTO studentDTO) {
        // PA dispensation für die rechnung irrelevant
        var electedModulesCreditSum = moduleElection.getElectedModules()
                .stream()
                .mapToInt(Module::getCredits)
                .sum();
        var sum = electedModulesCreditSum + studentDTO.getWpmDispensation();
        return sum == MAX_CREDITS_PER_YEAR_WITHOUT_PA_AND_BA;
    }

    private boolean isOverflownEmpty(ModuleElection moduleElection) {
        if(moduleElection.getOverflowedElectedModules() == null) {
            return false;
        }
        return moduleElection.getOverflowedElectedModules().size() == 0;
    }

}
