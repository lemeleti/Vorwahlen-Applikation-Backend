package ch.zhaw.vorwahlen.exporter;

import ch.zhaw.vorwahlen.model.modules.ModuleElection;

import java.util.Set;

/**
 * Interface which defines a contract for the output format.
 */
public interface ModuleElectionExporter {
    /**
     * Exports the module election from the provided list as byte array.
     * @param electionSet list containing all module elections
     * @return byte array which can either be sent via http or saved in a file.
     */
    byte[] export(Set<ModuleElection> electionSet);
}
