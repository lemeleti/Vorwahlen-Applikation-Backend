package ch.zhaw.vorwahlen.modulevalidation;

import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import ch.zhaw.vorwahlen.model.modules.Student;

import java.util.Map;

public class PartTimeElectionValidator extends AbstractElectionValidator {

    public static final int MAX_CREDITS_FIRST_ELECTION = 12;
    public static final int MAX_CREDITS_SECOND_ELECTION_WITHOUT_PA_AND_BA = 30; // PA = 6 Credits, BA = 12 Credits

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
        return isOverflownEmpty(election)
                && canModuleBeSelectedInThisRun(election)
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
        // todo fragen: 1. wahl CCP1, MC1 / 2. wahl CCP2, MC2 ---> currently this is invalid
        return !getStudent().isSecondElection()
                || consecutiveMap.size() > 0
                || containsSpecialConsecutiveModules(moduleElection);
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
        return validInterdisciplinaryModuleElection(moduleElection, neededInterdisciplinaryModules);
    }

    @Override
    protected boolean validSubjectModuleElection(ModuleElection moduleElection) {
        // todo fragen: warum 7 bei der zweiten wahl?
        // todo fragen: warum 5 wenn dispensiert?
        // IT18 Teilzeit: Zusammen mit den oben gewählten konsekutiven Modulen, wählen Sie total sieben Module (mit genehmigter Dispensation aus der beruflichen Anrechnung fünf Module)
        // IT19 Teilzeit: Zusammen mit den oben gewählten konsekutiven Modulen wählen Sie total zwei Module. In der Regel wählen Sie hier also kein Modul.
        var neededSubjectModules = getStudent().isSecondElection()
                ? NUM_SUBJECT_MODULES_SECOND_ELECTION
                : NUM_SUBJECT_MODULES_FIRST_ELECTION;
        return validSubjectModuleElection(moduleElection, neededSubjectModules);
    }

    @Override
    protected boolean validContextModuleElection(ModuleElection moduleElection) {
        // IT18 Teilzeit: Dies gehört zur Modulgruppe IT4. Sie können bis zu zwei dieser Module wählen. Das dritte Modul ist ein METU-Modul, das können Sie hier noch nicht vorwählen.
        // IT19 Teilzeit: Dies gehört zur Modulgruppe IT5. Sie können bis zu zwei dieser Module für das kommende Studienjahr wählen.
        var neededContextModules = getStudent().isSecondElection()
                ? NUM_CONTEXT_MODULES_SECOND_ELECTION
                : NUM_CONTEXT_MODULES_FIRST_ELECTION;
        return validContextModuleElection(moduleElection, neededContextModules);
    }

    @Override
    protected boolean isCreditSumValid(ModuleElection moduleElection) {
        var neededCredits = getStudent().isSecondElection()
                ? MAX_CREDITS_SECOND_ELECTION_WITHOUT_PA_AND_BA
                : MAX_CREDITS_FIRST_ELECTION;
        return isCreditSumValid(moduleElection, neededCredits);
    }

}
