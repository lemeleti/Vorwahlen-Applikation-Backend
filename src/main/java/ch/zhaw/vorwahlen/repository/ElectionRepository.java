package ch.zhaw.vorwahlen.repository;

import ch.zhaw.vorwahlen.model.core.module.Module;
import ch.zhaw.vorwahlen.model.core.election.Election;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository for the {@link Election}.
 */
@Repository
public interface ElectionRepository extends JpaRepository<Election, Long> {

    @Query("""
    SELECT e
    FROM Election e
    LEFT JOIN FETCH e.student
    LEFT JOIN FETCH e.electedModules
    """)
    Set<Election> findAllModules();

    @Query("""
    SELECT e
    FROM Election e
    LEFT JOIN FETCH e.student
    LEFT JOIN FETCH e.electedModules
    WHERE e.id = :id
    """)
    Optional<Election> findElectionById(@Param("id") long id);

    @Query("""
    SELECT e
    FROM Election e
    LEFT JOIN FETCH e.student
    LEFT JOIN FETCH e.electedModules
    WHERE e.student.email = :email
    """)
    Optional<Election> findElectionByStudent(@Param("email") String email);

    List<Election> findAllByElectedModulesContaining(Module module);
}
