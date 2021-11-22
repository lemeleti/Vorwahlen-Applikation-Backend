package ch.zhaw.vorwahlen.modulevalidation;

import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import ch.zhaw.vorwahlen.model.modules.Student;

import java.util.Map;
import java.util.stream.Collectors;

public class FullTimeElectionValidator extends AbstractElectionValidator {

    public static final int MAX_CREDITS_PER_YEAR_WITHOUT_PA_AND_BA = 42; // PA = 6 Credits, BA = 12 Credits
    public static final int NUM_CONTEXT_MODULES = 3;
    public static final int NUM_SUBJECT_MODULES = 8;
    public static final int NUM_INTERDISCIPLINARY_MODULES = 1;

    public FullTimeElectionValidator(Student student) {
        super(student);
    }

    @Override
    public boolean validate(ModuleElection election) {
        return isOverflownEmpty(election)
                && isCreditSumValid(election)
                && validContextModuleElection(election)
                && validSubjectModuleElection(election)
                && validInterdisciplinaryModuleElection(election)
                && validIpModuleElection(election)
                && validConsecutiveModulePairsInElection(election);
    }

    @Override
    protected boolean consecutiveModuleExtraChecks(ModuleElection moduleElection, Map<Module, Module> consecutiveMap) {
        // IT19 Vollzeit: Wählen Sie mindestens zweimal zwei konsekutive Module
        // Die Module PSPP und FUP werden auch als konsekutive Module anerkannt.
        return consecutiveMap.size() != 0
                && (consecutiveMap.size() >= 2 || containsSpecialConsecutiveModules(moduleElection));
    }

    @Override
    protected boolean validIpModuleElection(ModuleElection moduleElection)  {
        var isValid = true;
        if(getStudent().isIP()) {
            var englishModules = moduleElection.getElectedModules().stream()
                    .filter(module -> "Englisch".equals(module.getLanguage()))
                    .collect(Collectors.toSet());

            var creditSum = englishModules.stream()
                    .mapToInt(Module::getCredits)
                    .sum();

            var isEnglishCreditSumValid = creditSum + getStudent().getWpmDispensation() >= NUM_ENGLISH_CREDITS;
            var doesElectionContainModuleICAM = englishModules.stream()
                    .filter(module -> module.getShortModuleNo().contains("WVK.ICAM-EN"))
                    .count() == 1;

            isValid = isEnglishCreditSumValid && doesElectionContainModuleICAM;
        }
        return isValid;
    }

    @Override
    protected boolean validInterdisciplinaryModuleElection(ModuleElection moduleElection) {
        // IT19 Vollzeit: Sie müssen eines dieser Wahlmodule wählen (Sie können auch mehrere wählen, angerechnet werden kann aber nur ein Wahlmodul).
        return validInterdisciplinaryModuleElection(moduleElection, NUM_INTERDISCIPLINARY_MODULES);
    }

    @Override
    protected boolean validSubjectModuleElection(ModuleElection moduleElection) {
        // IT 19 Vollzeit: Zusammen mit den oben gewählten konsekutiven Modulen wählen Sie total acht Module.
        return validSubjectModuleElection(moduleElection, NUM_SUBJECT_MODULES);
    }

    @Override
    protected boolean validContextModuleElection(ModuleElection moduleElection) {
        // IT19 Vollzeit: Dies gehört zur Modulgruppe IT5. Sie können bis zu drei dieser Module wählen.
        return validContextModuleElection(moduleElection, NUM_CONTEXT_MODULES);
    }

    @Override
    protected boolean isCreditSumValid(ModuleElection moduleElection) {
        return isCreditSumValid(moduleElection, MAX_CREDITS_PER_YEAR_WITHOUT_PA_AND_BA);
    }

}
