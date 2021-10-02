package ch.zhaw.vorwahlen.model.modules;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
public class EventoData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String shortDescription;
    private String coordinator;
    @Column(columnDefinition = "blob")
    private String learningObjectives;
    @Column(columnDefinition = "mediumblob")
    private String moduleContents;
    private String literature;
    private String suppLiterature;
    private String prerequisites;
    @Column(columnDefinition = "blob")
    private String moduleStructure;
    private String language;
    @Column(columnDefinition = "blob")
    private String exams;
    private String remarks;
}
