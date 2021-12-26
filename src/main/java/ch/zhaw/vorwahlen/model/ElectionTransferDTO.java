package ch.zhaw.vorwahlen.model;

import ch.zhaw.vorwahlen.model.core.election.ElectionStatusDTO;
import ch.zhaw.vorwahlen.model.modulestructure.ElectionStructureDTO;

/**
 * Dto wrapper for {@link ElectionStructureDTO}, {@link ElectionStatusDTO},
 * if the save action succeeded and if the election is valid.
 */
public record ElectionTransferDTO(ElectionStructureDTO electionStructure,
                                  ElectionStatusDTO electionStatusDTO,
                                  boolean electionSaved,
                                  boolean electionValid) {}
