package ch.zhaw.vorwahlen.modulevalidation;

import ch.zhaw.vorwahlen.model.modules.ModuleElection;

public interface ElectionValidator {
    boolean validate(ModuleElection election);
}
