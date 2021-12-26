package ch.zhaw.vorwahlen.exporter;

import ch.zhaw.vorwahlen.model.core.election.Election;

import java.util.Set;

/**
 * Interface which defines a contract for the output format.
 */
public interface ElectionExporter {
    /**
     * Exports the election from the provided list as byte array.
     * @param electionSet list containing all elections.
     * @return byte array which can either be sent via http or saved in a file.
     */
    byte[] export(Set<Election> electionSet);
}
