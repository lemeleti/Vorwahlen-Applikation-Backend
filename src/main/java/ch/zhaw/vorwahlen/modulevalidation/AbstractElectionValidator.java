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
        var consecutiveMap = new HashMap<Module, Module>();
        for(var m1: moduleElection.getElectedModules()) {
            for(var m2: moduleElection.getElectedModules()) {
                if(!m1.equals(m2)
                        && !consecutiveMap.containsValue(m1)
                        && areModulesConsecutive(m1, m2)) {
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

    protected abstract boolean validIpModuleElection(ModuleElection moduleElection);

    protected long countModuleCategory(ModuleElection moduleElection, ModuleCategory moduleCategory) {
        return moduleElection.getElectedModules()
                .stream()
                .map(module -> ModuleCategory.parse(module.getModuleNo(), module.getModuleGroup()))
                .filter(category -> category == moduleCategory)
                .count();
    }

    protected abstract boolean validInterdisciplinaryModuleElection(ModuleElection moduleElection);

    protected abstract boolean validSubjectModuleElection(ModuleElection moduleElection);

    protected abstract boolean validContextModuleElection(ModuleElection moduleElection);

    protected abstract boolean isCreditSumValid(ModuleElection moduleElection);

    protected boolean isOverflownEmpty(ModuleElection moduleElection) {
        if(moduleElection.getOverflowedElectedModules() == null) {
            return true;
        }
        return moduleElection.getOverflowedElectedModules().size() == 0;
    }
}
