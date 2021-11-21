package ch.zhaw.vorwahlen.model.modules;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;

@Entity
@Table(name = "classes")
@Data
public class StudentClass {
    @Id
    private String name;
    @OneToMany(mappedBy = "studentClass")
    private Set<Student> students;
}
