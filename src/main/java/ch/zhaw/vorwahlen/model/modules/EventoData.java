package ch.zhaw.vorwahlen.model.modules;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Objects;

/**
 * Model / Entity class for the additional module data
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
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        var that = (EventoData) o;
        return moduleNo != null && Objects.equals(moduleNo, that.moduleNo);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
