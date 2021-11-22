package ch.zhaw.vorwahlen.modulevalidation;

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
    public static final int NUM_ENGLISH_CREDITS = 20;
    public static final int CREDIT_PER_SUBJECT_MODULE = 4;
    private final Student student;


    protected boolean validConsecutiveModulePairsInElection(ModuleElection moduleElection) {
        var consecutiveMap = calculateConsecutiveMap(moduleElection);

        var countConsecutiveMissingPart = consecutiveMap.values().stream()
                .filter(Objects::isNull)
                .count();

        return countConsecutiveMissingPart == 0 && consecutiveModuleExtraChecks(moduleElection, consecutiveMap);
    }

    protected Map<Module, Module> calculateConsecutiveMap(ModuleElection moduleElection) {
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

        // remove one fo the duplicate entries -> k: AI1, v: AI2 / k: AI2, v: AI1
        consecutiveMap.entrySet().removeIf(entrySet -> consecutiveMap.containsValue(entrySet.getKey()));
        return consecutiveMap;
    }

    protected abstract boolean consecutiveModuleExtraChecks(ModuleElection moduleElection, Map<Module, Module> consecutiveMap);

    protected boolean containsSpecialConsecutiveModules(ModuleElection moduleElection) {
        return containsModule(moduleElection.getElectedModules(), "WV.PSPP")
                && containsModule(moduleElection.getElectedModules(), "WV.FUP");
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

    protected long countModuleCategory(ModuleElection moduleElection, ModuleCategory moduleCategory) {
        return moduleElection.getElectedModules()
                .stream()
                .map(module -> ModuleCategory.parse(module.getModuleNo(), module.getModuleGroup()))
                .filter(category -> category == moduleCategory)
                .count();
    }

    protected boolean isOverflownEmpty(ModuleElection moduleElection) {
        if(moduleElection.getOverflowedElectedModules() == null) {
            return true;
        }
        return moduleElection.getOverflowedElectedModules().size() == 0;
    }

    protected abstract boolean validIpModuleElection(ModuleElection moduleElection);

    protected abstract boolean validInterdisciplinaryModuleElection(ModuleElection moduleElection);

    protected boolean validInterdisciplinaryModuleElection(ModuleElection moduleElection, int neededInterdisciplinaryModules) {
        var count = countModuleCategory(moduleElection, ModuleCategory.INTERDISCIPLINARY_MODULE);
        return count == neededInterdisciplinaryModules;
    }

    protected abstract boolean validSubjectModuleElection(ModuleElection moduleElection);

    protected boolean validSubjectModuleElection(ModuleElection moduleElection, int neededSubjectModules) {
        var count = countModuleCategory(moduleElection, ModuleCategory.SUBJECT_MODULE);
        var dispensCount = getStudent().getWpmDispensation() / CREDIT_PER_SUBJECT_MODULE;
        return count + dispensCount == neededSubjectModules;
    }

    protected abstract boolean validContextModuleElection(ModuleElection moduleElection);

    protected boolean validContextModuleElection(ModuleElection moduleElection, int neededContextModules) {
        var count = countModuleCategory(moduleElection, ModuleCategory.CONTEXT_MODULE);
        return count == neededContextModules;
    }

    protected abstract boolean isCreditSumValid(ModuleElection moduleElection);

    protected boolean isCreditSumValid(ModuleElection moduleElection, int neededCredits) {
        // PA dispensation für die rechnung irrelevant
        var electedModulesCreditSum = moduleElection.getElectedModules()
                .stream()
                .mapToInt(Module::getCredits)
                .sum();
        var sum = electedModulesCreditSum + getStudent().getWpmDispensation();
        return sum == neededCredits;
    }

}
