package ch.zhaw.vorwahlen.model.core.election;

import ch.zhaw.vorwahlen.model.core.module.ModuleCategory;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public record ElectionStatusElementDTO(ModuleCategory moduleCategory,
                                       boolean isValid,
                                       @JsonInclude(JsonInclude.Include.NON_NULL) List<String> reasons) {}
