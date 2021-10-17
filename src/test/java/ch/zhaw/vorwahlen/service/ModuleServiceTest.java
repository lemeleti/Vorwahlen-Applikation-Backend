package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.repository.EventoDataRepository;
import ch.zhaw.vorwahlen.repository.ModuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
class ModuleServiceTest {
    private final ModuleRepository moduleRepository;
    private final EventoDataRepository eventoDataRepository;
    private ModuleService moduleService;

    @Autowired
    public ModuleServiceTest(ModuleRepository moduleRepository, EventoDataRepository eventoDataRepository) {
        this.moduleRepository = moduleRepository;
        this.eventoDataRepository = eventoDataRepository;
    }

    @BeforeEach
    void setUp() {
        moduleService = new ModuleService(moduleRepository, eventoDataRepository);
    }

    @Test
    @Sql("classpath:sql/modules.sql")
    void testGetAllModules() {
        assertFalse(moduleService.getAllModules().isEmpty());
    }

}
