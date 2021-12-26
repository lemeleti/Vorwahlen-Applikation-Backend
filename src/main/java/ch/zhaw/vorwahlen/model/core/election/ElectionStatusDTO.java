package ch.zhaw.vorwahlen.model.core.election;

/**
 * Dto for the election status
 */
public record ElectionStatusDTO(ElectionStatusElementDTO subjectValidation,
                                ElectionStatusElementDTO contextValidation,
                                ElectionStatusElementDTO interdisciplinaryValidation,
                                ElectionStatusElementDTO additionalValidation) {}
