package ch.zhaw.vorwahlen.repository;

import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import ch.zhaw.vorwahlen.model.modules.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for the module election.
 */
@Repository
public interface ElectionRepository extends JpaRepository<ModuleElection, Long> {
}
