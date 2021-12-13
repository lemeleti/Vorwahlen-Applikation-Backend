package ch.zhaw.vorwahlen.repository;

import ch.zhaw.vorwahlen.model.modules.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link Module}.
 */
@Repository
public interface ModuleRepository extends JpaRepository<Module, String> {

}
