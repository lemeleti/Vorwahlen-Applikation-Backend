package ch.zhaw.vorwahlen.model.evento;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Objects;

/**
 * Model / Entity class for the additional module data.
 */
@Entity
@Getter @Setter
@NoArgsConstructor
public class EventoData {

    @Id
    private String moduleNo;

    @Column(columnDefinition = "text")
    private String shortDescription;
    private String coordinator;
    @Column(columnDefinition = "blob")
    private String learningObjectives;
    @Column(columnDefinition = "mediumblob")
    private String moduleContents;
    @Column(columnDefinition = "blob")
    private String literature;
    @Column(columnDefinition = "blob")
    private String suppLiterature;
    @Column(columnDefinition = "blob")
    private String prerequisites;
    @Column(columnDefinition = "blob")
    private String moduleStructure;
    @Column(columnDefinition = "blob")
    private String exams;
    @Column(columnDefinition = "blob")
    private String remarks;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventoData that)) return false;
        return Objects.equals(getModuleNo(), that.getModuleNo())
                && Objects.equals(getShortDescription(), that.getShortDescription())
                && Objects.equals(getCoordinator(), that.getCoordinator())
                && Objects.equals(getLearningObjectives(), that.getLearningObjectives())
                && Objects.equals(getModuleContents(), that.getModuleContents())
                && Objects.equals(getLiterature(), that.getLiterature())
                && Objects.equals(getSuppLiterature(), that.getSuppLiterature())
                && Objects.equals(getPrerequisites(), that.getPrerequisites())
                && Objects.equals(getModuleStructure(), that.getModuleStructure())
                && Objects.equals(getExams(), that.getExams())
                && Objects.equals(getRemarks(), that.getRemarks());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getModuleNo(), getShortDescription(), getCoordinator(), getLearningObjectives(),
                            getModuleContents(), getLiterature(), getSuppLiterature(), getPrerequisites(),
                            getModuleStructure(), getExams(), getRemarks());
    }

}
