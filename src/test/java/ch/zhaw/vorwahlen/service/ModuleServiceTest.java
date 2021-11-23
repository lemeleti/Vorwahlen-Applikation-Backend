package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.model.dto.ModuleDTO;
import ch.zhaw.vorwahlen.model.modules.ModuleCategory;
import ch.zhaw.vorwahlen.repository.EventoDataRepository;
import ch.zhaw.vorwahlen.repository.ModuleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ModuleServiceTest {

    private static final String MODULE_LIST_FILE_NAME = "Liste_alle_Module_SM2025_SGL_Def_1.7-2021-03-29.xlsx";
    private static final String WORK_SHEET_NAME = "Module 2025";
    private static final String MULTIPART_FILE_REQUEST_PARAMETER = "file";

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

    @AfterEach
    void tearDown() {
        moduleRepository.deleteAll();
        eventoDataRepository.deleteAll();
    }

    @Test
    @Sql("classpath:sql/modules.sql")
    void testGetAllModules() {
        var result = moduleService.getAllModules(null);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(75, result.size());
    }

    @Test
    void testImportModuleExcel() throws IOException {
        // prepare
        var fis = getClass().getClassLoader().getResourceAsStream(MODULE_LIST_FILE_NAME);
        var mockMultipartFile = new MockMultipartFile(MULTIPART_FILE_REQUEST_PARAMETER, MODULE_LIST_FILE_NAME, "", fis);

        // execute
        assertDoesNotThrow(() -> moduleService.importModuleExcel(mockMultipartFile, WORK_SHEET_NAME));

        // verify
        var result = moduleService.getAllModules(null);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(75, result.size());
        assertEquals(24, result.stream().filter(moduleDTO -> !moduleDTO.getConsecutiveModuleNo().isBlank()).count());
    }

    @Test
    @Sql("classpath:sql/modules.sql")
    void testGetModuleById() {
        var expected = ModuleDTO.builder()
                .executionSemester(new ModuleDTO.ExecutionSemester(List.of(5), List.of(7)))
                .moduleNo("t.BA.WM.DHEAL-EN.19HS")
                .category(ModuleCategory.INTERDISCIPLINARY_MODULE)
                .credits((byte) 4)
                .language("Englisch")
                .moduleTitle("Digital Health")
                .consecutiveModuleNo("")
                .build();
        var result = moduleService.getModuleById(expected.getModuleNo());
        assertTrue(result.isPresent());
        assertEquals(expected, result.get());
    }

    @Test
    void testGetModuleById_Null() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> moduleService.getModuleById(null));
    }

    @Test
    void testGetModuleById_Not_Existing() {
        var result = assertDoesNotThrow(() -> moduleService.getModuleById("invalid"));
        assertFalse(result.isPresent());
    }

    @Disabled
    @Test
    @Sql("classpath:sql/")
    void testGetEventoDataById() {
        // todo: create sql data
    }

}
