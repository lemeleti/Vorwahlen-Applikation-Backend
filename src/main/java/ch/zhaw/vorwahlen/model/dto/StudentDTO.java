package ch.zhaw.vorwahlen.model.dto;

import ch.zhaw.vorwahlen.model.modules.StudentClass;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for a module
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
@Data
public class StudentDTO {
    private String email;
    private String name;
    @JsonProperty("class")
    private StudentClass clazz;
    @JsonProperty("dispensation_pa")
    private int paDispensation;
    @JsonProperty("dispensation_wpm")
    private int wpmDispensation;
    @JsonProperty("is_ip")
    private boolean isIP;
    @JsonProperty("is_tz")
    private boolean isTZ;
}
