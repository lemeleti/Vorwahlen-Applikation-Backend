package ch.zhaw.vorwahlen.model.core.election;

import ch.zhaw.vorwahlen.model.core.student.Student;
import ch.zhaw.vorwahlen.model.core.validationsetting.ValidationSetting;
import ch.zhaw.vorwahlen.model.core.module.Module;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;

/**
 * Model / Entity class for a module election.
 */
@Entity
@Getter @Setter
@NoArgsConstructor
public class ModuleElection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @OneToOne(cascade = CascadeType.PERSIST, orphanRemoval = true)
    private ValidationSetting validationSetting;

    @Column(columnDefinition = "tinyint(1) default 0")
    private boolean isElectionValid;

    @ManyToMany
    @JoinTable(name  = "elected_modules",
            joinColumns = @JoinColumn(name = "election_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "module_no", referencedColumnName = "moduleNo"))
    private Set<Module> electedModules;

    public void removeModuleFromElection(Module module) {
        electedModules.remove(module);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ModuleElection that)) return false;
        return isElectionValid() == that.isElectionValid()
                && Objects.equals(getId(), that.getId())
                && Objects.equals(getStudent(), that.getStudent())
                && Objects.equals(getValidationSetting(), that.getValidationSetting())
                && Objects.equals(getElectedModules(), that.getElectedModules());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getStudent(), getValidationSetting(), isElectionValid(), getElectedModules());
    }

}
