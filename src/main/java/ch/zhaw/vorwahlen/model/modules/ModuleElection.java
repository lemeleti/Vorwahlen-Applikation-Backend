package ch.zhaw.vorwahlen.model.modules;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
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

    private boolean isElectionValid;

    @OneToMany
    @JoinTable(name  = "elected_modules",
            joinColumns = @JoinColumn(name = "election_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "module_no", referencedColumnName = "moduleNo"))
    private Set<Module> electedModules;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ModuleElection that = (ModuleElection) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
