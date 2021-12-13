package ch.zhaw.vorwahlen.repository;

import ch.zhaw.vorwahlen.model.modules.StudentClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link StudentClass}.
 */
@Repository
public interface StudentClassRepository extends JpaRepository<StudentClass, String> {
}
