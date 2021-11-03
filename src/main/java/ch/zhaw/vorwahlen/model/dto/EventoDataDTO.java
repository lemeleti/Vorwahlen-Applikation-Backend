package ch.zhaw.vorwahlen.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Data
@Builder
public class EventoDataDTO {
    @JsonProperty("short_description")
    private String shortDescription;
    private String coordinator;
    @JsonProperty("learning_objectives")
    private String learningObjectives;
    @JsonProperty("module_contents")
    private String moduleContents;
    private String literature;
    @JsonProperty("supp_literature")
    private String suppLiterature;
    private String prerequisites;
    @JsonProperty("module_structure")
    private String moduleStructure;
    private String language;
    private String exams;
    private String remarks;
}
