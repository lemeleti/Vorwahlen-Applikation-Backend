package ch.zhaw.vorwahlen.repository;

import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import ch.zhaw.vorwahlen.model.modules.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for the module election.
 */
@Repository
public interface ElectionRepository extends JpaRepository<ModuleElection, Long> {

    @Query("SELECT e from ModuleElection e JOIN FETCH e.student WHERE e.student.email = :email")
    Optional<ModuleElection> findModuleElectionByStudent(@Param("email") String email);
}
