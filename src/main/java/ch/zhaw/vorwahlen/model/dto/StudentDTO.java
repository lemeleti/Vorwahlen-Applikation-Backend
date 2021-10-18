package ch.zhaw.vorwahlen.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO for a module
 */
@AllArgsConstructor
@Data
public class StudentDTO {
    private String email;
    private String name;
    @JsonProperty("class")
    private String clazz;
    @JsonProperty("dispensation_pa")
    private int paDispensation;
    @JsonProperty("dispensation_wpm")
    private int wpmDispensation;
}
