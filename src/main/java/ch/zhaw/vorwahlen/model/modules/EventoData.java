package ch.zhaw.vorwahlen.model.modules;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Model / Entity class for the additional module data
 */
@Entity
@Data
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
}
