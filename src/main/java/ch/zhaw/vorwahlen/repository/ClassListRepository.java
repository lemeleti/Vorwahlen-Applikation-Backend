package ch.zhaw.vorwahlen.repository;

import ch.zhaw.vorwahlen.model.modules.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for the class list.
 */
@Repository
public interface ClassListRepository extends JpaRepository<Student, String> {
}
