package ch.zhaw.vorwahlen.model.modules;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "classes")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "students")
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
        System.out.println("STUDENT CLASS HIBERNATE CHECK" + (Hibernate.getClass(this) != Hibernate.getClass(o)));
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        var that = (StudentClass) o;
        return name != null && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
