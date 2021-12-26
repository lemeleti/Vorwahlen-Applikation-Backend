package ch.zhaw.vorwahlen.validation;

import ch.zhaw.vorwahlen.model.core.election.ElectionStatus;
import ch.zhaw.vorwahlen.model.core.election.Election;

/**
 * Contract for a validator.
 */
public interface ElectionValidator {

    /**
     * Validate the elected modules.
     * @param election the {@link Election}
     * @return ElectionStatus
     */
    ElectionStatus validate(Election election);
}
