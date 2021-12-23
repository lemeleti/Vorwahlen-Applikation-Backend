package ch.zhaw.vorwahlen.validation;

import ch.zhaw.vorwahlen.model.core.election.ModuleElectionStatus;
import ch.zhaw.vorwahlen.model.core.election.ModuleElection;

/**
 * Contract for a validator.
 */
public interface ElectionValidator {

    /**
     * Validate the elected modules.
     * @param election the {@link ModuleElection}
     * @return ModuleElectionStatus
     */
    ModuleElectionStatus validate(ModuleElection election);
}
