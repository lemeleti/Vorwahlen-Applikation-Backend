package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.exception.StudentNotFoundException;
import ch.zhaw.vorwahlen.mapper.Mapper;
import ch.zhaw.vorwahlen.model.dto.StudentDTO;
import ch.zhaw.vorwahlen.model.modules.Student;
import ch.zhaw.vorwahlen.repository.ClassListRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
class StudentServiceTest {

    private static final String CLASS_LIST_FILE_NAME = "Vorlage_Klassenzuteilungen.xlsx";
    private static final String WORK_SHEET_NAME = "Sheet1";
    private static final String MULTIPART_FILE_REQUEST_PARAMETER = "file";

    private final ClassListRepository classListRepository;
    private final Mapper<StudentDTO, Student> mapper;
    private StudentService studentService;

    @Autowired
    public StudentServiceTest(ClassListRepository classListRepository, Mapper<StudentDTO, Student> mapper) {
        this.classListRepository = classListRepository;
        this.mapper = mapper;
    }

    @BeforeEach
    void setUp() {
        studentService = new StudentService(classListRepository, mapper);
    }

    @AfterEach
    void tearDown() {
        classListRepository.deleteAll();
    }

    @Test
    @Sql("classpath:sql/class_list.sql")
    void testGetAllClassLists() {
        var result = studentService.getAllClassLists();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(202, result.size());
    }

    @Test
    @Sql("classpath:sql/class_list.sql")
    void testGetStudentById() {
        var studentId = "meierbob@students.zhaw.ch";
        var result = studentService.getStudentById(studentId);
        assertEquals(studentId, result.getEmail());
    }

    @Test
    @Sql("classpath:sql/class_list.sql")
    void testGetStudentById_Null() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> studentService.getStudentById(null));
    }

    @Test
    @Sql("classpath:sql/class_list.sql")
    void testGetStudentById_Blank() {
        assertThrows(StudentNotFoundException.class, () -> studentService.getStudentById(" "));
    }

    @Test
    void testImportClassListExcel() throws IOException {
        // prepare
        var fis = getClass().getClassLoader().getResourceAsStream(CLASS_LIST_FILE_NAME);
        var mockMultipartFile = new MockMultipartFile(MULTIPART_FILE_REQUEST_PARAMETER, CLASS_LIST_FILE_NAME, "", fis);

        // execute
        assertDoesNotThrow(() -> studentService.importClassListExcel(mockMultipartFile, WORK_SHEET_NAME));

        // verify
        var result = studentService.getAllClassLists();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
    }
}
