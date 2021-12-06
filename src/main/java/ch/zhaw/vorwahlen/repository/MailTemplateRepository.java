package ch.zhaw.vorwahlen.repository;

import ch.zhaw.vorwahlen.model.modules.MailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MailTemplateRepository extends JpaRepository<MailTemplate, Long> {
}
