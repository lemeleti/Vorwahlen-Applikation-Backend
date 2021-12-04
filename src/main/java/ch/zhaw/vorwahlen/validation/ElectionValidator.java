package ch.zhaw.vorwahlen.validation;

import ch.zhaw.vorwahlen.model.modules.ModuleElectionStatus;
import ch.zhaw.vorwahlen.model.modules.ModuleElection;

public interface ElectionValidator {
    ModuleElectionStatus validate(ModuleElection election);
}
