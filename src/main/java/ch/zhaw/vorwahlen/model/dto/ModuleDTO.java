package ch.zhaw.vorwahlen.model.dto;

import ch.zhaw.vorwahlen.model.modules.ModuleCategory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;



/**
 * DTO for a module
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@Builder
public class ModuleDTO {
    public static record ExecutionSemester(List<Integer> fullTimeSemesterList,
                                    List<Integer> partTimeSemesterList) {}

    private String moduleNo;
    private String moduleTitle;
    private ModuleCategory category;
    private byte credits;
    private String language;
    private ExecutionSemester executionSemester;
    // todo maybe link with another module
    private String consecutiveModuleNo;
}
