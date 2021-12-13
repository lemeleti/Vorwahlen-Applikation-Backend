package ch.zhaw.vorwahlen.model.modules;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
 * Model / Entity class for a student.
 */
@Entity
@Table(name = "students")
@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Student {

    @Id
    private String email;
    private String name;
    @ManyToOne(cascade = CascadeType.MERGE)
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
    @Column(columnDefinition = "tinyint(1) default 1")
    private boolean firstTimeSetup;
    @Column(columnDefinition = "tinyint(1) default 1")
    private boolean canElect;

    @OneToOne(cascade = CascadeType.MERGE, orphanRemoval = true)
    @JoinColumn(name = "election_id")
    private ModuleElection election;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student that)) return false;
        return getPaDispensation() == that.getPaDispensation()
                && getWpmDispensation() == that.getWpmDispensation()
                && isIP() == that.isIP() && isTZ() == that.isTZ()
                && isSecondElection() == that.isSecondElection()
                && isFirstTimeSetup() == that.isFirstTimeSetup()
                && isCanElect() == that.isCanElect()
                && Objects.equals(getEmail(), that.getEmail())
                && Objects.equals(getName(), that.getName())
                && Objects.equals(getStudentClass(), that.getStudentClass());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEmail(), getName(), getStudentClass(), getPaDispensation(), getWpmDispensation(), isIP(),
                            isTZ(), isSecondElection(), isFirstTimeSetup(), isCanElect());
    }

}
