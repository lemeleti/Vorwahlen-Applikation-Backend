package ch.zhaw.vorwahlen.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

/**
 * DTO for a module
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
@Data
public class StudentDTO {
    @Email(message = "{validation.email.invalid}")
    private String email;
    @NotEmpty(message = "{validation.name.empty}")
    private String name;
    @NotEmpty(message = "{validation.class.empty}")
    @JsonProperty("class")
    private String clazz;
    @Min(value = 0, message = "{validation.pa.dispensation.invalid}")
    @Max(value = 6, message = "{validation.pa.dispensation.invalid}")
    private int paDispensation;
    @Min(value = 0, message = "{validation.wpm.dispensation.invalid}")
    @Max(value = 8, message = "{validation.wpm.dispensation.invalid}")
    private int wpmDispensation;
    @NotNull(message = "{validation.ip.null}")
    private boolean isIP;
    @NotNull(message = "{validation.tz.null}")
    private boolean isTZ;
}
