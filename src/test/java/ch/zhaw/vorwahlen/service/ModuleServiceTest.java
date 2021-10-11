package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.repository.EventoDataRepository;
import ch.zhaw.vorwahlen.repository.ModuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
class ModuleServiceTest {
    private final Environment env;
    private final ModuleRepository moduleRepository;
    private final EventoDataRepository eventoDataRepository;
    private ModuleService moduleService;

    @Autowired
    public ModuleServiceTest(Environment env, ModuleRepository moduleRepository, EventoDataRepository eventoDataRepository) {
        this.env = env;
        this.moduleRepository = moduleRepository;
        this.eventoDataRepository = eventoDataRepository;
    }

    @BeforeEach
    void setUp() {
        moduleService = new ModuleService(moduleRepository, eventoDataRepository, env);
    }

    @Test
    @Sql("classpath:sql/modules.sql")
    void testGetAllModules() {
        assertFalse(moduleService.getAllModules().isEmpty());
    }

}
