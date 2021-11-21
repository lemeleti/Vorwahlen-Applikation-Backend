package ch.zhaw.vorwahlen.model.modules;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.Set;

/**
 * Model / Entity class for a module election.
 */
@Entity
@Data
@NoArgsConstructor
public class ModuleElection {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
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

    @OneToMany
    @JoinTable(name  = "overflowed_elected_modules",
            joinColumns = @JoinColumn(name = "election_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "module_no", referencedColumnName = "moduleNo"))
    private Set<Module> overflowedElectedModules;

}
