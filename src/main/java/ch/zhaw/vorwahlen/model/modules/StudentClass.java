package ch.zhaw.vorwahlen.model.modules;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Objects;
import java.util.Set;

/**
 * Model / Entity class for the classes.
 */
@Entity
@Table(name = "classes")
@Getter @Setter
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StudentClass that)) return false;
        return Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }

}
