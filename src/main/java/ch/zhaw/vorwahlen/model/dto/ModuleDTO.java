package ch.zhaw.vorwahlen.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO for a module
 */
@AllArgsConstructor
@Data
public class ModuleDTO {
    @JsonProperty("module_no")
    private String moduleNo;
    @JsonProperty("module_title")
    private String moduleTitle;
    private String language;
}
