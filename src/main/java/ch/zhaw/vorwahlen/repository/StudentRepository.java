package ch.zhaw.vorwahlen.repository;

import ch.zhaw.vorwahlen.model.modules.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link Student}.
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, String> {
    @Query("""
    SELECT s
    FROM Student s
    WHERE s.election.isElectionValid = :electionStatus
    """)
    List<Student> getAllByElectionStatus(@Param("electionStatus") boolean electionStatus);
}
