package ch.zhaw.vorwahlen.parser;

import ch.zhaw.vorwahlen.model.modules.Student;
import ch.zhaw.vorwahlen.model.modules.StudentClass;
import ch.zhaw.vorwahlen.model.modules.parser.StudentLookupTable;
import ch.zhaw.vorwahlen.repository.StudentRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DataJpaTest
class ClassListParserTest {

    private static final String CLASS_LIST_FILE_NAME = "Vorlage_Klassenzuteilungen.xlsx";
    private static final String WORK_SHEET_NAME = "Sheet1";
    private static final String DEFAULT_CELL_VALUE = "valid content";

    ClassListParser classListParser;

    @Autowired
    StudentRepository studentRepository;

    @Mock Row rowMock;
    @Mock Cell defaultCellMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        classListParser = new ClassListParser(getClass().getClassLoader().getResourceAsStream(CLASS_LIST_FILE_NAME), WORK_SHEET_NAME);
    }

    @AfterEach
    void tearDown() {
        for (var field: StudentLookupTable.values()) {
            field.setCellNumber(-1);
        }
    }

    /* **************************************************************************************************************
     * Positive tests
     * ************************************************************************************************************** */

    @Test
    @Sql("classpath:sql/class_list_test_parse.sql")
    void parseModulesFromXLSX() throws IOException {
        // prepare
        var expected = sortByEmail(studentRepository.findAll());

        // execute
        var result = sortByEmail(classListParser.parseFromXLSX());

        // verify
        assertNotNull(result);
        assertEquals(expected.size(), result.size());
        assertIterableEquals(expected, result);
    }

    private List<Student> sortByEmail(List<Student> list) {
        return list.stream()
                .sorted(Comparator.comparing(Student::getEmail))
                .toList();
    }

    @Test
    void createObjectFromRow() {
        // prepare
        setupLookupTable();

        var expected = Student.builder()
                .email(DEFAULT_CELL_VALUE)
                .name(DEFAULT_CELL_VALUE)
                .studentClass(new StudentClass(DEFAULT_CELL_VALUE))
                .canElect(true)
                .firstTimeSetup(true)
                .build();

        when(rowMock.getCell(anyInt())).thenReturn(defaultCellMock);
        when(defaultCellMock.toString()).thenReturn(DEFAULT_CELL_VALUE);

        // execute
        var result = classListParser.createObjectFromRow(rowMock);

        // verify
        assertNotNull(result);
        assertEquals(expected, result);
        verify(rowMock, times(3)).getCell(anyInt());
    }

    private void setupLookupTable() {
        int i = 0;
        for (var field: StudentLookupTable.values()) {
            field.setCellNumber(i++);
        }
    }

    /* **************************************************************************************************************
     * Negative tests
     * ************************************************************************************************************** */

    @Test
    void createObjectFromRow_Null() {
        assertThrows(NullPointerException.class, () -> classListParser.createObjectFromRow(null));
    }

    @Test
    void createObjectFromRow_Cell_Header_Not_Found() {
        when(rowMock.getCell(anyInt())).thenReturn(defaultCellMock);
        when(rowMock.getCell(-1)).thenReturn(null);
        assertThrows(NullPointerException.class, () -> classListParser.createObjectFromRow(rowMock));
        verify(rowMock, times(1)).getCell(-1);
    }

}