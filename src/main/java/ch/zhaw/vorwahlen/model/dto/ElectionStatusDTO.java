package ch.zhaw.vorwahlen.model.dto;

public record ElectionStatusDTO(ElectionStatusElementDTO subjectValidation,
                                ElectionStatusElementDTO contextValidation,
                                ElectionStatusElementDTO interdisciplinaryValidation,
                                ElectionStatusElementDTO additionalValidation) {}
