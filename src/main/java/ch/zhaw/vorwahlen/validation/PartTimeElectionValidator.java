package ch.zhaw.vorwahlen.validation;

import ch.zhaw.vorwahlen.config.ResourceBundleMessageLoader;
import ch.zhaw.vorwahlen.model.core.module.Module;
import ch.zhaw.vorwahlen.model.core.module.ModuleCategory;
import ch.zhaw.vorwahlen.model.core.election.Election;
import ch.zhaw.vorwahlen.model.core.student.Student;

import java.util.Map;
import java.util.Objects;

/**
 * Concrete validator for parttime students.
 */
public class PartTimeElectionValidator extends AbstractElectionValidator {

    public static final int MIN_CREDITS_FIRST_ELECTION = 8;
    public static final int MIN_CREDITS_SECOND_ELECTION_WITHOUT_PA_AND_BA = 28; // PA = 6 Credits, BA = 12 Credits

    public static final int NUM_CONTEXT_MODULES_FIRST_ELECTION = 2;
    public static final int NUM_CONTEXT_MODULES_SECOND_ELECTION = 1;

    public static final int NUM_SUBJECT_MODULES_FIRST_ELECTION = 2;
    public static final int NUM_SUBJECT_MODULES_SECOND_ELECTION = 6;

    public static final int NUM_INTERDISCIPLINARY_MODULES_FIRST_ELECTION = 0;
    public static final int NUM_INTERDISCIPLINARY_MODULES_SECOND_ELECTION = 1;

    public PartTimeElectionValidator(Student student) {
        super(student);
    }

    @Override
    protected boolean consecutiveModuleExtraChecks(Election election, Map<Module, Module> consecutiveMap) {
        // IT18 Teilzeit: Wenn Sie im aktuellen Studienjahr schon zwei konsekutive Module belegt haben, wählen Sie mindestens einmal zwei konsekutive Module, ansonsten mindestens zweimal zwei konsekutive Module.
        // IT19 Teilzeit: Wählen Sie bis zu zwei konsekutive Module (empfohlen: zwei Module). Achten Sie speziell auf die nötigen Vorkenntnisse der Module.
        if(!getStudent().isSecondElection()) {
            return true;
        }

        var settings = election.getValidationSetting();
        if (settings.isSkipConsecutiveModuleCheck()) {
            // case: 1. wahl CCP1, MC1 / 2. wahl CCP2, MC2, ...
            return true;
        }

        var countConsecutivePairs = consecutiveMap.values().stream()
                .filter(Objects::nonNull)
                .count();

        countConsecutivePairs += countSpecialConsecutiveModulePairs(election);

        var isValid = false;
        if (settings.hadAlreadyElectedTwoConsecutiveModules()) {
            // case: 1. Wahl  CCP1, CCP2 / 2. Wahl MC1, MC2, ... oder FUP, PSPP, ...
            isValid = countConsecutivePairs > 0;
            if (!isValid) {
                var reason = String.format(ResourceBundleMessageLoader.getMessage("election_status.too_less_consecutive"), MISSING_1_CONSECUTIVE_PAIR);
                getElectionStatus().getSubjectValidation().addReason(reason);
            }
        } else {
            /*
             * case: 1. Wahl SCAD-EN, RAP-EN  / 2. Wahl CCP1, CCP2, MC1, MC2, ...
             * oder
             * case: 1. Wahl SCAD-EN, RAP-EN  / 2. Wahl CCP1, CCP2, FUP, PSPP, ...
             */
            isValid = countConsecutivePairs > 1;
            if (!isValid) {
                var missingPairs = countConsecutivePairs == 0 ? MISSING_2_CONSECUTIVE_PAIRS : MISSING_1_CONSECUTIVE_PAIR;
                var reason = String.format(ResourceBundleMessageLoader.getMessage("election_status.too_less_consecutive"), missingPairs);
                getElectionStatus().getSubjectValidation().addReason(reason);
            }
        }

        return isValid;
    }

    @Override
    protected boolean validIpElection(Election election) {
        // NOTE: IP not checked because we don't store the elected modules from the previous year.
        return true;
    }

    @Override
    protected boolean validInterdisciplinaryElection(Election election) {
        // IT18 Teilzeit: Sie können bis zu einem der oben gewählten Wahlpflichtmodule durch ein Wahlmodule ersetzen.
        // IT19 Teilzeit: In der Regel wählen Sie jetzt noch keines dieser Wahlmodule, da es mit grosser Wahrscheinlichkeit mit Ihrem obligatorischen Stundenplan nicht kompatibel ist.
        var neededInterdisciplinaryModules = getStudent().isSecondElection()
                ? NUM_INTERDISCIPLINARY_MODULES_SECOND_ELECTION
                : NUM_INTERDISCIPLINARY_MODULES_FIRST_ELECTION;
        return validElectionCountByCategory(election, neededInterdisciplinaryModules, ModuleCategory.INTERDISCIPLINARY_MODULE);
    }

    @Override
    protected boolean validSubjectElection(Election election) {
        // IT18 Teilzeit: Zusammen mit den oben gewählten konsekutiven Modulen, wählen Sie total sieben Module (mit genehmigter Dispensation aus der beruflichen Anrechnung fünf Module)
        // IT19 Teilzeit: Zusammen mit den oben gewählten konsekutiven Modulen wählen Sie total zwei Module. In der Regel wählen Sie hier also kein Modul.
        var dispensCount = 0;
        var neededSubjectModules = NUM_SUBJECT_MODULES_FIRST_ELECTION;

        if(getStudent().isSecondElection()) {
            dispensCount = getStudent().getWpmDispensation() / CREDIT_PER_SUBJECT_MODULE;
            neededSubjectModules = NUM_SUBJECT_MODULES_SECOND_ELECTION;
        }
        var count = dispensCount + countModuleCategory(election, ModuleCategory.SUBJECT_MODULE);
        var isValid = count == neededSubjectModules;
        if(!isValid) {
            addReasonWhenCountByCategoryNotValid(ModuleCategory.SUBJECT_MODULE, getElectionStatus().getSubjectValidation(), count, neededSubjectModules);
        }
        return isValid;
    }

    @Override
    protected boolean validContextElection(Election election) {
        var totalNumContextModules = NUM_CONTEXT_MODULES_FIRST_ELECTION + NUM_CONTEXT_MODULES_SECOND_ELECTION;
        var count = countModuleCategory(election, ModuleCategory.CONTEXT_MODULE);

        var isValid = count >= NUM_CONTEXT_MODULES_FIRST_ELECTION && count <= totalNumContextModules;

        if(getStudent().isSecondElection()) {
            count += election.getValidationSetting().getElectedContextModulesInFirstElection();
            isValid = count == totalNumContextModules;
        }

        if(!isValid) {
            if(!getStudent().isSecondElection()) {
                totalNumContextModules = NUM_CONTEXT_MODULES_FIRST_ELECTION;
            }
            addReasonWhenCountByCategoryNotValid(ModuleCategory.CONTEXT_MODULE, getElectionStatus().getContextValidation(), count, totalNumContextModules);
        }
        return isValid;
    }

    @Override
    protected boolean isCreditSumValid(Election election) {
        // apply context modules
        var firstElectionContextCredits = NUM_CONTEXT_MODULES_FIRST_ELECTION * CREDITS_PER_CONTEXT_MODULE;
        var secondElectionContextCredits = NUM_CONTEXT_MODULES_SECOND_ELECTION * CREDITS_PER_CONTEXT_MODULE;
        var totalContextCredits = firstElectionContextCredits + secondElectionContextCredits;

        var minNeededCredits = MIN_CREDITS_FIRST_ELECTION + firstElectionContextCredits;
        var maxNeededCredits = MIN_CREDITS_FIRST_ELECTION + totalContextCredits;
        var dispensation = 0;

        if(getStudent().isSecondElection()) {
            var contextCreditsAlreadyDone = election.getValidationSetting().getElectedContextModulesInFirstElection()
                    * CREDITS_PER_CONTEXT_MODULE;
            if(totalContextCredits >= contextCreditsAlreadyDone) {
                totalContextCredits -= contextCreditsAlreadyDone;
            }
            minNeededCredits = MIN_CREDITS_SECOND_ELECTION_WITHOUT_PA_AND_BA + totalContextCredits;
            maxNeededCredits = MIN_CREDITS_SECOND_ELECTION_WITHOUT_PA_AND_BA + totalContextCredits;
            dispensation = getStudent().getWpmDispensation();
        }

        // validate
        var sum = sumCreditsInclusiveDispensation(election, dispensation);
        addReasonWhenCreditSumNotValid(sum, minNeededCredits, maxNeededCredits);
        return sum >= minNeededCredits && sum <= maxNeededCredits;
    }

}
