package ch.zhaw.vorwahlen.repository;

import ch.zhaw.vorwahlen.model.core.validationsetting.ValidationSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repository for {@link ValidationSetting}.
 */
public interface ValidationSettingRepository extends JpaRepository<ValidationSetting, Long> {
    @Query("""
    SELECT s.election.validationSetting
    FROM Student s
    WHERE s.email = :email
    """)
    Optional<ValidationSetting> findValidationSettingByStudentMail(@Param("email") String email);
}
