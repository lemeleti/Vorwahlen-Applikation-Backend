package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.model.modules.Student;
import ch.zhaw.vorwahlen.repository.ClassListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class DispensationServiceTest {

    private static final String DISPENSATION_FILE_NAME = "Vorlage_Dispensationen.xlsx";
    private static final String WORK_SHEET_NAME = "Sheet1";
    private static final String MULTIPART_FILE_REQUEST_PARAMETER = "file";

    private final ClassListRepository classListRepository;
    private DispensationService dispensationService;

    @Autowired
    public DispensationServiceTest(ClassListRepository classListRepository) {
        this.classListRepository = classListRepository;
    }

    @BeforeEach
    void setUp() {
        dispensationService = new DispensationService(classListRepository);
    }

    @Test
    @Sql("classpath:sql/class_list_test_parse.sql")
    void testImportDispensationExcel() throws IOException {
        // prepare
        var expected = List.of(
                Student.builder().name("Anna Muster").email("musteranna@students.zhaw.ch").clazz("IT19a_WIN").paDispensation(6).build(),
                Student.builder().name("Bob Meier").email("meierbob@students.zhaw.ch").clazz("IT19ta_WIN").wpmDispensation(8).build()
        );
        var fis = getClass().getClassLoader().getResourceAsStream(DISPENSATION_FILE_NAME);
        var mockMultipartFile = new MockMultipartFile(MULTIPART_FILE_REQUEST_PARAMETER, DISPENSATION_FILE_NAME, "", fis);

        // execute
        assertDoesNotThrow(() -> dispensationService.importDispensationExcel(mockMultipartFile, WORK_SHEET_NAME));

        // verify
        var result = classListRepository.findAll();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertIterableEquals(sortByEmail(expected), sortByEmail(result));
    }

    private List<Student> sortByEmail(List<Student> list) {
        return list.stream().sorted(Comparator.comparing(Student::getEmail)).toList();
    }

}
