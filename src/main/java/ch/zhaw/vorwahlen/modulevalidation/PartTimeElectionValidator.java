package ch.zhaw.vorwahlen.modulevalidation;

import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleCategory;
import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import ch.zhaw.vorwahlen.model.modules.Student;

import java.util.Map;
import java.util.Objects;

public class PartTimeElectionValidator extends AbstractElectionValidator {

    public static final int MIN_CREDITS_FIRST_ELECTION = 8;
    public static final int MIN_CREDITS_SECOND_ELECTION_WITHOUT_PA_AND_BA = 28; // PA = 6 Credits, BA = 12 Credits

    public static final int NUM_CONTEXT_MODULES_FIRST_ELECTION = 2;
    public static final int NUM_CONTEXT_MODULES_SECOND_ELECTION = 1;

    public static final int NUM_SUBJECT_MODULES_FIRST_ELECTION = 2;
    public static final int NUM_SUBJECT_MODULES_SECOND_ELECTION = 6;

    public static final int NUM_INTERDISCIPLINARY_MODULES_FIRST_ELECTION = 0;
    public static final int NUM_INTERDISCIPLINARY_MODULES_SECOND_ELECTION = 1;

    protected static final String[] SECOND_ELECTION_SEMESTERS = { "7", "8" };
    protected static final String[] FIRST_ELECTION_SEMESTERS = { "5", "6" };

    public PartTimeElectionValidator(Student student) {
        super(student);
    }

    @Override
    public boolean validate(ModuleElection election) {
        if(election.getValidationSetting().isRepetent()) return true;
        return canModuleBeSelectedInThisRun(election)
                && isCreditSumValid(election)
                && validContextModuleElection(election)
                && validSubjectModuleElection(election)
                && validInterdisciplinaryModuleElection(election)
                && validIpModuleElection(election)
                && validConsecutiveModulePairsInElection(election);
    }

    protected boolean canModuleBeSelectedInThisRun(ModuleElection moduleElection) {
        return getStudent().isSecondElection()
                ? containsAnyValidSemesterInEveryElectedModule(moduleElection, SECOND_ELECTION_SEMESTERS)
                : containsAnyValidSemesterInEveryElectedModule(moduleElection, FIRST_ELECTION_SEMESTERS);
    }

    private boolean containsAnyValidSemesterInEveryElectedModule(ModuleElection moduleElection, String[] possibleSemesters) {
        for (var m: moduleElection.getElectedModules()) {
            var containsAnySemester = false;

            var i = 0;
            while(!containsAnySemester && i < possibleSemesters.length) {
                containsAnySemester = m.getPartTimeSemester().contains(possibleSemesters[i]);
                i++;
            }

            if (!containsAnySemester) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean consecutiveModuleExtraChecks(ModuleElection moduleElection, Map<Module, Module> consecutiveMap) {
        // IT18 Teilzeit: Wenn Sie im aktuellen Studienjahr schon zwei konsekutive Module belegt haben, wählen Sie mindestens einmal zwei konsekutive Module, ansonsten mindestens zweimal zwei konsekutive Module.
        // IT19 Teilzeit: Wählen Sie bis zu zwei konsekutive Module (empfohlen: zwei Module). Achten Sie speziell auf die nötigen Vorkenntnisse der Module.
        if(!getStudent().isSecondElection()) {
            return true;
        }

        var settings = moduleElection.getValidationSetting();
        if (settings.isSkipConsecutiveModuleCheck()) {
            // case: 1. wahl CCP1, MC1 / 2. wahl CCP2, MC2, ...
            return true;
        }

        var countConsecutivePairs = consecutiveMap.values().stream()
                .filter(Objects::nonNull)
                .count();

        var isValid = false;
        if (settings.hadAlreadyElectedTwoConsecutiveModules()) {
            // case: 1. Wahl  CCP1, CCP2 / 2. Wahl MC1, MC2, ... oder FUP, PSPP, ...
            isValid = countConsecutivePairs > 0 || containsSpecialConsecutiveModules(moduleElection);
        } else {
            /*
             * case: 1. Wahl SCAD-EN, RAP-EN  / 2. Wahl CCP1, CCP2, MC1, MC2, ...
             * oder
             * case: 1. Wahl SCAD-EN, RAP-EN  / 2. Wahl CCP1, CCP2, FUP, PSPP, ...
             */
            isValid = countConsecutivePairs > 1 || countConsecutivePairs == 1 && containsSpecialConsecutiveModules(moduleElection);
        }

        return isValid;
    }

    @Override
    protected boolean validIpModuleElection(ModuleElection moduleElection) {
        // NOTE: IP not checked because we don't store the elected modules from the previous year.
        return true;
    }

    @Override
    protected boolean validInterdisciplinaryModuleElection(ModuleElection moduleElection) {
        // IT18 Teilzeit: Sie können bis zu einem der oben gewählten Wahlpflichtmodule durch ein Wahlmodule ersetzen.
        // IT19 Teilzeit: In der Regel wählen Sie jetzt noch keines dieser Wahlmodule, da es mit grosser Wahrscheinlichkeit mit Ihrem obligatorischen Stundenplan nicht kompatibel ist.
        var neededInterdisciplinaryModules = getStudent().isSecondElection()
                ? NUM_INTERDISCIPLINARY_MODULES_SECOND_ELECTION
                : NUM_INTERDISCIPLINARY_MODULES_FIRST_ELECTION;
        return validModuleElectionCountByCategory(moduleElection, neededInterdisciplinaryModules, ModuleCategory.INTERDISCIPLINARY_MODULE);
    }

    @Override
    protected boolean validSubjectModuleElection(ModuleElection moduleElection) {
        // IT18 Teilzeit: Zusammen mit den oben gewählten konsekutiven Modulen, wählen Sie total sieben Module (mit genehmigter Dispensation aus der beruflichen Anrechnung fünf Module)
        // IT19 Teilzeit: Zusammen mit den oben gewählten konsekutiven Modulen wählen Sie total zwei Module. In der Regel wählen Sie hier also kein Modul.
        var count = countModuleCategory(moduleElection, ModuleCategory.SUBJECT_MODULE);
        var dispensCount = 0;
        var neededSubjectModules = NUM_SUBJECT_MODULES_FIRST_ELECTION;

        if(getStudent().isSecondElection()) {
            dispensCount = getStudent().getWpmDispensation() / CREDIT_PER_SUBJECT_MODULE;
            neededSubjectModules = NUM_SUBJECT_MODULES_SECOND_ELECTION;
        }

        return count + dispensCount == neededSubjectModules;
    }

    @Override
    protected boolean validContextModuleElection(ModuleElection moduleElection) {
        // NOTE: Context not checked because we don't store the elected modules from the previous year.
        var totalNumContextModules = NUM_CONTEXT_MODULES_FIRST_ELECTION + NUM_CONTEXT_MODULES_SECOND_ELECTION;
        var count = countModuleCategory(moduleElection, ModuleCategory.CONTEXT_MODULE);
        return count >= 0 && count <= totalNumContextModules;
    }

    @Override
    protected boolean isCreditSumValid(ModuleElection moduleElection) {
        var totalNumContext = NUM_CONTEXT_MODULES_FIRST_ELECTION + NUM_CONTEXT_MODULES_SECOND_ELECTION;

        var minNeededCredits = MIN_CREDITS_FIRST_ELECTION;
        var maxNeededCredits = MIN_CREDITS_FIRST_ELECTION + totalNumContext * CREDITS_PER_CONTEXT_MODULE;
        var dispensation = 0;

        if(getStudent().isSecondElection()) {
            minNeededCredits = MIN_CREDITS_SECOND_ELECTION_WITHOUT_PA_AND_BA;
            maxNeededCredits = MIN_CREDITS_SECOND_ELECTION_WITHOUT_PA_AND_BA + totalNumContext * CREDITS_PER_CONTEXT_MODULE;
            dispensation = getStudent().getWpmDispensation();
        }

        var sum = sumCreditsInclusiveDispensation(moduleElection, dispensation);
        return sum >= minNeededCredits && sum <= maxNeededCredits;
    }

}
