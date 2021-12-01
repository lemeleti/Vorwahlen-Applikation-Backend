package ch.zhaw.vorwahlen.modulevalidation;

import ch.zhaw.vorwahlen.model.modules.ElectionStatus;
import ch.zhaw.vorwahlen.model.modules.ModuleElection;

public interface ElectionValidator {
    ElectionStatus validate(ModuleElection election);
}
