package ch.zhaw.vorwahlen.model.dto;

/**
 * Dto wrapper for {@link ElectionStructureDTO}, {@link ElectionStatusDTO},
 * if the save action succeeded and if the election is valid.
 */
public record ElectionTransferDTO(ElectionStructureDTO electionStructure,
                                  ElectionStatusDTO electionStatusDTO,
                                  boolean electionSaved,
                                  boolean electionValid) {}
