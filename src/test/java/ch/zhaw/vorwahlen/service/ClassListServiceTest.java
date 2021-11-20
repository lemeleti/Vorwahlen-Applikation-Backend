package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.repository.ClassListRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ClassListServiceTest {

    private static final String CLASS_LIST_FILE_NAME = "Vorlage_Klassenzuteilungen.xlsx";
    private static final String WORK_SHEET_NAME = "Sheet1";
    private static final String MULTIPART_FILE_REQUEST_PARAMETER = "file";

    private final ClassListRepository classListRepository;
    private ClassListService classListService;

    @Autowired
    public ClassListServiceTest(ClassListRepository classListRepository) {
        this.classListRepository = classListRepository;
    }

    @BeforeEach
    void setUp() {
        classListService = new ClassListService(classListRepository);
    }

    @AfterEach
    void tearDown() {
        classListRepository.deleteAll();
    }

    @Test
    @Sql("classpath:sql/class_list.sql")
    void testGetAllClassLists() {
        var result = classListService.getAllClassLists();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(202, result.size());
    }

    @Test
    @Sql("classpath:sql/class_list.sql")
    void testGetStudentById() {
        var result = classListService.getStudentById("meierbob@students.zhaw.ch");
        assertTrue(result.isPresent());
    }

    @Test
    @Sql("classpath:sql/class_list.sql")
    void testGetStudentById_Null() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> classListService.getStudentById(null));
    }

    @Test
    @Sql("classpath:sql/class_list.sql")
    void testGetStudentById_Blank() {
        var result = assertDoesNotThrow(() -> classListService.getStudentById("  "));
        assertTrue(result.isEmpty());
    }

    @Test
    void testImportClassListExcel() throws IOException {
        // prepare
        var fis = getClass().getClassLoader().getResourceAsStream(CLASS_LIST_FILE_NAME);
        var mockMultipartFile = new MockMultipartFile(MULTIPART_FILE_REQUEST_PARAMETER, CLASS_LIST_FILE_NAME, "", fis);

        // execute
        assertDoesNotThrow(() -> classListService.importClassListExcel(mockMultipartFile, WORK_SHEET_NAME));

        // verify
        var result = classListService.getAllClassLists();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
    }
}
