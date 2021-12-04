package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.exception.StudentNotFoundException;
import ch.zhaw.vorwahlen.mapper.Mapper;
import ch.zhaw.vorwahlen.model.dto.StudentDTO;
import ch.zhaw.vorwahlen.model.modules.Student;
import ch.zhaw.vorwahlen.model.modules.StudentClass;
import ch.zhaw.vorwahlen.repository.ClassListRepository;
import ch.zhaw.vorwahlen.repository.StudentClassRepository;
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
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
class StudentServiceTest {

    private static final String CLASS_LIST_FILE_NAME = "Vorlage_Klassenzuteilungen.xlsx";
    private static final String DISPENSATION_FILE_NAME = "Vorlage_Dispensationen.xlsx";
    private static final String WORK_SHEET_NAME = "Sheet1";
    private static final String MULTIPART_FILE_REQUEST_PARAMETER = "file";

    private final ClassListRepository classListRepository;
    private final StudentClassRepository studentClassRepository;
    private final Mapper<StudentDTO, Student> mapper;
    private StudentService studentService;

    @Autowired
    public StudentServiceTest(ClassListRepository classListRepository,
                              StudentClassRepository studentClassRepository,
                              Mapper<StudentDTO, Student> mapper) {
        this.classListRepository = classListRepository;
        this.studentClassRepository = studentClassRepository;
        this.mapper = mapper;
    }

    @BeforeEach
    void setUp() {
        studentService = new StudentService(classListRepository, studentClassRepository, mapper);
    }

    @AfterEach
    void tearDown() {
        classListRepository.deleteAll();
    }

    @Test
    @Sql("classpath:sql/class_list.sql")
    void testGetAllClassLists() {
        var result = studentService.getAllStudents();
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
        var result = studentService.getAllStudents();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
    }

    @Test
    @Sql("classpath:sql/class_list_test_parse.sql")
    void testImportDispensationExcel() throws IOException {
        // prepare
        var vzClass = new StudentClass();
        var tzClass = new StudentClass();
        vzClass.setName("IT19a_WIN");
        tzClass.setName("IT19ta_WIN");
        // ch.zhaw.vorwahlen.model.modules.Student                        @53ef0db3<Student(email=meierbob@students.zhaw.ch, name=Bob Meier, studentClass=StudentClass(name=IT19ta_WIN), paDispensation=0, wpmDispensation=8, isIP=false, isTZ=true, isSecondElection=false, election=null)>
        // ch.zhaw.vorwahlen.model.modules.Student$HibernateProxy$DiB75Mal@29957fe0<Student(email=meierbob@students.zhaw.ch, name=Bob Meier, studentClass=StudentClass(name=IT19ta_WIN), paDispensation=0, wpmDispensation=8, isIP=false, isTZ=true, isSecondElection=false, election=null)>
        var expected = List.of(
                Student.builder().name("Anna Muster").studentClass(vzClass).email("musteranna@students.zhaw.ch").paDispensation(6).build(),
                Student.builder().name("Bob Meier").studentClass(tzClass).email("meierbob@students.zhaw.ch").wpmDispensation(8).isTZ(true).build()
        );
        var fis = getClass().getClassLoader().getResourceAsStream(DISPENSATION_FILE_NAME);
        var mockMultipartFile = new MockMultipartFile(MULTIPART_FILE_REQUEST_PARAMETER, DISPENSATION_FILE_NAME, "", fis);

        // execute
        assertDoesNotThrow(() -> studentService.importDispensationExcel(mockMultipartFile, WORK_SHEET_NAME));

        // verify
        var result = classListRepository.findAll();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        areListsEqual(sortByEmail(expected), sortByEmail(result));
    }

    private List<Student> sortByEmail(List<Student> list) {
        return list.stream().sorted(Comparator.comparing(Student::getEmail)).toList();
    }

    private void areListsEqual(List<Student> students1, List<Student> students2) {
        // todo: investigate why assertIterEquals does not wok anymore
        assertEquals(students1.size(), students2.size());
        for (int i = 0; i < students1.size() && i < students2.size(); i++) {
            var student1 = students1.get(i);
            var student2 = students2.get(i);

            assertEquals(student1.getEmail(), student2.getEmail());
            assertEquals(student1.getName(), student2.getName());
            assertEquals(student1.getStudentClass().getName(), student2.getStudentClass().getName());
            assertEquals(student1.getWpmDispensation(), student2.getWpmDispensation());
            assertEquals(student1.isTZ(), student2.isTZ());
        }
    }
}
