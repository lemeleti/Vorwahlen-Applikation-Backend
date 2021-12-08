package ch.zhaw.vorwahlen.repository;

import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

/**
 * Repository for the {@link ModuleElection}.
 */
@Repository
public interface ElectionRepository extends JpaRepository<ModuleElection, Long> {

    @Query("""
    SELECT e
    FROM ModuleElection e
    LEFT JOIN FETCH e.student
    LEFT JOIN FETCH e.electedModules
    """)
    Set<ModuleElection> findAllModules();

    @Query("""
    SELECT e
    FROM ModuleElection e
    LEFT JOIN FETCH e.student
    LEFT JOIN FETCH e.electedModules
    WHERE e.id = :id
    """)
    Optional<ModuleElection> findModuleElectionById(@Param("id") long id);

    @Query("""
    SELECT e
    FROM ModuleElection e
    LEFT JOIN FETCH e.student
    LEFT JOIN FETCH e.electedModules
    WHERE e.student.email = :email
    """)
    Optional<ModuleElection> findModuleElectionByStudent(@Param("email") String email);
}
