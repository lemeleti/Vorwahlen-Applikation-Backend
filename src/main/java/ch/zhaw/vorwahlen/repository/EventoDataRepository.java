package ch.zhaw.vorwahlen.repository;

import ch.zhaw.vorwahlen.model.modules.EventoData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventoDataRepository extends JpaRepository<EventoData, Long> {
}
