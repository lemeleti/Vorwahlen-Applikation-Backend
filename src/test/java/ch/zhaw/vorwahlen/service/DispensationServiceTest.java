package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.model.modules.Student;
import ch.zhaw.vorwahlen.model.modules.StudentClass;
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
        assertDoesNotThrow(() -> dispensationService.importDispensationExcel(mockMultipartFile, WORK_SHEET_NAME));

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
