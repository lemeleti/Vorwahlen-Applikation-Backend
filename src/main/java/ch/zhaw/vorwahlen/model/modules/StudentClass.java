package ch.zhaw.vorwahlen.model.modules;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;

@Entity
@Table(name = "classes")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentClass {
    @Id
    private String name;
    @OneToMany(mappedBy = "studentClass")
    private Set<Student> students;

    public StudentClass(String name) {
        this.name = name;
    }
}
