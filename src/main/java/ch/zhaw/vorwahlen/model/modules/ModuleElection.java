package ch.zhaw.vorwahlen.model.modules;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.Set;

/**
 * Model / Entity class for a module election.
 */
@Entity
@Data
@NoArgsConstructor
public class ModuleElection {

    @Id
    private String studentEmail;

    private boolean isElectionValid;

    @OneToMany
    private Set<Module> electedModules;

    @OneToMany
    private Set<Module> overflowedElectedModules;

}
