package ch.zhaw.vorwahlen.modulevalidation;

import ch.zhaw.vorwahlen.model.dto.StudentDTO;
import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleCategory;
import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import ch.zhaw.vorwahlen.model.modules.Student;
import ch.zhaw.vorwahlen.service.ModuleService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@RequiredArgsConstructor
@Getter
public abstract class AbstractElectionValidator implements ElectionValidator {
    public static final int MAX_CREDITS_PER_YEAR_WITHOUT_PA_AND_BA = 42; // PA = 6 Credits, BA = 12 Credits
    public static final int NUM_CONTEXT_MODULES = 3;
    public static final int NUM_SUBJECT_MODULES = 8;
    public static final int NUM_INTERDISCIPLINARY_MODULES = 1;
    public static final int NUM_ENGLISH_CREDITS = 20;
    public static final int CREDIT_PER_SUBJECT_MODULE = 4;
    private final Student student;

    protected boolean validConsecutiveModuleElection(ModuleElection moduleElection) {
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

    protected boolean hasAtLeastTwoConsecutiveModules(ModuleElection moduleElection, Map<Module, Module> consecutiveMap) {
        return consecutiveMap.size() >= 2 ||
                (consecutiveMap.size() == 1
                        && containsModule(moduleElection.getElectedModules(), "WV.PSPP")
                        && containsModule(moduleElection.getElectedModules(), "WV.FUP"));
    }

    protected boolean areModulesConsecutive(Module m1, Module m2) {
        return m1.getConsecutiveModuleNo() != null && !m1.getConsecutiveModuleNo().isBlank()
                && m2.getConsecutiveModuleNo() != null && !m2.getConsecutiveModuleNo().isBlank();
    }

    protected boolean containsModule(Set<Module> modules, String moduleNo) {
        return modules.stream()
                .filter(module -> moduleNo.equals(module.getShortModuleNo()))
                .count() == 1;
    }

    protected boolean validIpModuleElection(ModuleElection moduleElection) {
        var isValid = true;
        if(student.isIP()) {
            var sum = moduleElection.getElectedModules().stream()
                    .filter(module -> "Englisch".equals(module.getLanguage()))
                    .mapToInt(Module::getCredits)
                    .sum();
            // todo: ask for dispensations in ip
            isValid = sum + student.getWpmDispensation() >= NUM_ENGLISH_CREDITS;
        }
        return isValid;
    }

    protected long countModuleCategory(ModuleElection moduleElection, ModuleCategory moduleCategory) {
        return moduleElection.getElectedModules()
                .stream()
                .map(module -> ModuleCategory.parse(module.getModuleNo(), module.getModuleGroup()))
                .filter(category -> category == moduleCategory)
                .count();
    }

    protected boolean validInterdisciplinaryModuleElection(ModuleElection moduleElection) {
        var count = countModuleCategory(moduleElection, ModuleCategory.INTERDISCIPLINARY_MODULE);
        return count == NUM_INTERDISCIPLINARY_MODULES;
    }

    protected boolean validSubjectModuleElection(ModuleElection moduleElection) {
        var count = countModuleCategory(moduleElection, ModuleCategory.SUBJECT_MODULE);
        var dispensCount = student.getWpmDispensation() / CREDIT_PER_SUBJECT_MODULE;
        return count + dispensCount == NUM_SUBJECT_MODULES;
    }

    protected boolean validContextModuleElection(ModuleElection moduleElection) {
        var count = countModuleCategory(moduleElection, ModuleCategory.CONTEXT_MODULE);
        return count == NUM_CONTEXT_MODULES;
    }

    protected boolean isCreditSumValid(ModuleElection moduleElection) {
        // PA dispensation für die rechnung irrelevant
        var electedModulesCreditSum = moduleElection.getElectedModules()
                .stream()
                .mapToInt(Module::getCredits)
                .sum();
        var sum = electedModulesCreditSum + student.getWpmDispensation();
        return sum == MAX_CREDITS_PER_YEAR_WITHOUT_PA_AND_BA;
    }

    protected boolean isOverflownEmpty(ModuleElection moduleElection) {
        if(moduleElection.getOverflowedElectedModules() == null) {
            return false;
        }
        return moduleElection.getOverflowedElectedModules().size() == 0;
    }
}
