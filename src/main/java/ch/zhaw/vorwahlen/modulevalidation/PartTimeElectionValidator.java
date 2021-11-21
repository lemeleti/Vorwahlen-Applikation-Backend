package ch.zhaw.vorwahlen.modulevalidation;

import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import ch.zhaw.vorwahlen.model.modules.Student;

public class PartTimeElectionValidator extends AbstractElectionValidator {
    public PartTimeElectionValidator(Student student) {
        super(student);
    }
    @Override
    public boolean validate(ModuleElection election) {
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
        return false;
    }

    @Override
    protected boolean validIpModuleElection(ModuleElection moduleElection) {
        return false;
    }

    @Override
    protected boolean validInterdisciplinaryModuleElection(ModuleElection moduleElection) {
        return false;
    }

    @Override
    protected boolean validSubjectModuleElection(ModuleElection moduleElection) {
        return false;
    }

    @Override
    protected boolean validContextModuleElection(ModuleElection moduleElection) {
        return false;
    }

    @Override
    protected boolean isCreditSumValid(ModuleElection moduleElection) {
        return false;
    }

}
