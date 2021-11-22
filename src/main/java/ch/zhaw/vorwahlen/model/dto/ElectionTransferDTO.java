package ch.zhaw.vorwahlen.model.dto;

public record ElectionTransferDTO(ElectionStructureDTO electionStructure,
                                  boolean electionSaved,
                                  boolean electionValid) {}
