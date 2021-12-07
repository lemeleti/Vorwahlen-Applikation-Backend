package ch.zhaw.vorwahlen.model.dto;

import ch.zhaw.vorwahlen.model.modules.ModuleCategory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



/**
 * DTO for a module
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@Builder
public class ModuleDTO {
    private String moduleNo;
    private String shortModuleNo;
    private String moduleTitle;
    private int moduleId;
    private String moduleGroup;
    private String institute;
    private ModuleCategory category;
    private byte credits;
    private String language;
    private int semester;
    // todo maybe link with another module
    private String consecutiveModuleNo;
}
