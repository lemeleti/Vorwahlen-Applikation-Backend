package ch.zhaw.vorwahlen.model.core.module;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Dto for the {@link Module}.
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@Builder
public class ModuleDTO {

    @NotEmpty(message = "{validation.module.moduleNo}")
    private String moduleNo;
    @NotEmpty(message = "{validation.module.shortModuleNo}")
    private String shortModuleNo;
    @NotEmpty(message = "{validation.module.moduleTitle}")
    private String moduleTitle;
    @NotNull(message = "{validation.module.moduleId}")
    private int moduleId;
    @NotEmpty(message = "{validation.module.moduleGroup}")
    private String moduleGroup;
    private String institute;
    private ModuleCategory category;
    @NotNull(message = "{validation.module.credits}")
    private byte credits;
    @NotEmpty(message = "{validation.module.language}")
    private String language;
    @NotNull(message = "{validation.module.semester}")
    private int semester;
    private String consecutiveModuleNo;

}
