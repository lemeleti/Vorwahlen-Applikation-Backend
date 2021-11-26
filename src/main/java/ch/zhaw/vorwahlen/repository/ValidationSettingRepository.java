package ch.zhaw.vorwahlen.repository;

import ch.zhaw.vorwahlen.model.modules.ValidationSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ValidationSettingRepository extends JpaRepository<ValidationSetting, Long> {

}
