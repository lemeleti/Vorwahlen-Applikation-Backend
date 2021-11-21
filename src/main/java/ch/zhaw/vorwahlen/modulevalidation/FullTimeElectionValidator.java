package ch.zhaw.vorwahlen.modulevalidation;

import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleCategory;
import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import ch.zhaw.vorwahlen.model.modules.Student;

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
    protected boolean validIpModuleElection(ModuleElection moduleElection)  {
        var isValid = true;
        if(getStudent().isIP()) {
            var englishModules = moduleElection.getElectedModules().stream()
                    .filter(module -> "Englisch".equals(module.getLanguage()))
                    .collect(Collectors.toSet());
            var sum = englishModules.stream()
                    .mapToInt(Module::getCredits)
                    .sum();
            // TODO: module ICAM has to be in selection (for fulltime) --> ask for part time
            var isEnglishCreditSumValid = sum + getStudent().getWpmDispensation() >= NUM_ENGLISH_CREDITS;
            var doesElectionContainModuleICAM = englishModules.stream()
                    .filter(module -> module.getShortModuleNo().contains("WVK.ICAM-EN"))
                    .count() == 1;

            isValid = isEnglishCreditSumValid && doesElectionContainModuleICAM;
        }
        return isValid;
    }

    @Override
    protected boolean validInterdisciplinaryModuleElection(ModuleElection moduleElection) {
        var count = countModuleCategory(moduleElection, ModuleCategory.INTERDISCIPLINARY_MODULE);
        return count == NUM_INTERDISCIPLINARY_MODULES;
    }

    @Override
    protected boolean validSubjectModuleElection(ModuleElection moduleElection) {
        var count = countModuleCategory(moduleElection, ModuleCategory.SUBJECT_MODULE);
        var dispensCount = getStudent().getWpmDispensation() / CREDIT_PER_SUBJECT_MODULE;
        return count + dispensCount == NUM_SUBJECT_MODULES;
    }

    @Override
    protected boolean validContextModuleElection(ModuleElection moduleElection) {
        var count = countModuleCategory(moduleElection, ModuleCategory.CONTEXT_MODULE);
        return count == NUM_CONTEXT_MODULES;
    }

    @Override
    protected boolean isCreditSumValid(ModuleElection moduleElection) {
        // PA dispensation für die rechnung irrelevant
        var electedModulesCreditSum = moduleElection.getElectedModules()
                .stream()
                .mapToInt(Module::getCredits)
                .sum();
        var sum = electedModulesCreditSum + getStudent().getWpmDispensation();
        return sum == MAX_CREDITS_PER_YEAR_WITHOUT_PA_AND_BA;
    }

}
