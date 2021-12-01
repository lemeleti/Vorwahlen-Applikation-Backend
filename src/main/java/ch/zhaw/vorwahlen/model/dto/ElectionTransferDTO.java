package ch.zhaw.vorwahlen.model.dto;

public record ElectionTransferDTO(ElectionStructureDTO electionStructure,
                                  ElectionStatusDTO electionStatusDTO,
                                  boolean electionSaved,
                                  boolean electionValid) {}
