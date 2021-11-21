package ch.zhaw.vorwahlen.model.modules;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Objects;

/**
 * Model / Entity class for a class list entry
 */
@Entity
@Table(name = "students")
@Getter @Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class Student {

    @Id
    private String email;
    private String name;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "class_name")
    private StudentClass studentClass;

    @Column(columnDefinition = "integer default 0")
    private int paDispensation;
    @Column(columnDefinition = "integer default 0")
    private int wpmDispensation;
    @Column(columnDefinition = "tinyint(1) default 0")
    private boolean isIP;
    @Column(columnDefinition = "tinyint(1) default 0")
    private boolean isTZ;
    @Column(columnDefinition = "tinyint(1) default 0")
    private boolean isSecondElection;
    @OneToOne
    @JoinColumn(name = "election_id")
    private ModuleElection election;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        System.out.println("STUDENT HIBERNATE CHECK" + (Hibernate.getClass(this) != Hibernate.getClass(o)));
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        var student = (Student) o;
        return email != null && Objects.equals(email, student.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}
