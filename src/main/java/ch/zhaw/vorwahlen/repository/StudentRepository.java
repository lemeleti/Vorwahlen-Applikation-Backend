package ch.zhaw.vorwahlen.repository;

import ch.zhaw.vorwahlen.model.core.student.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

/**
 * Repository for {@link Student}.
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, String> {
    @Transactional
    @Modifying
    @Query("UPDATE Student s set s.canElect = false")
    void closeElection();
}
