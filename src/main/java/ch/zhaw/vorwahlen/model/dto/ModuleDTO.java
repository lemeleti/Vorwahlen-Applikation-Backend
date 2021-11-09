package ch.zhaw.vorwahlen.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("module_no")
    private String moduleNo;
    @JsonProperty("short_module_no")
    private String shortModuleNo;
    @JsonProperty("module_title")
    private String moduleTitle;
    @JsonProperty("module_group")
    private String moduleGroup;
    @JsonProperty("is_ip_module")
    private boolean isIPModule;
    private String institute;
    private byte credits;
    private String language;
    @JsonProperty("full_time_semester_list")
    private List<Integer> fullTimeSemesterList;
    @JsonProperty("part_time_semester_list")
    private List<Integer> partTimeSemesterList;
    @JsonProperty("consecutive_module_no")
    private String consecutiveModuleNo;
}
