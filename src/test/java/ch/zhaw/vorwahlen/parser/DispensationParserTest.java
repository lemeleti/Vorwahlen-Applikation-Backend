package ch.zhaw.vorwahlen.parser;

import ch.zhaw.vorwahlen.model.modules.DispensationLookupTable;
import ch.zhaw.vorwahlen.model.modules.Student;
import ch.zhaw.vorwahlen.repository.ClassListRepository;
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
class DispensationParserTest {

    private static final String DISPENSATION_FILE_NAME = "Vorlage_Dispensationen.xlsx";
    private static final String WORK_SHEET_NAME = "Sheet1";
    private static final String EMAIL_CELL_VALUE = "valid content";
    private static final double DEFAULT_CELL_VALUE = 0;

    DispensationParser dispensationParser;

    @Autowired
    ClassListRepository classListRepository;

    @Mock Row rowMock;
    @Mock Cell defaultCellMock;
    @Mock Cell emailCellMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dispensationParser = new DispensationParser(getClass().getClassLoader().getResourceAsStream(DISPENSATION_FILE_NAME), WORK_SHEET_NAME);
    }

    @AfterEach
    void tearDown() {
        for (var field: DispensationLookupTable.values()) {
            field.setCellNumber(-1);
        }
    }

    /* **************************************************************************************************************
     * Positive tests
     * ************************************************************************************************************** */

    @Test
    @Sql("classpath:sql/dispensation_list_test_parse.sql")
    void parseDispensationFromXLSX() throws IOException {
        // prepare
        var expected = sortByEmail(classListRepository.findAll());

        // execute
        var result = sortByEmail(dispensationParser.parseModulesFromXLSX());

        // verify
        assertNotNull(result);
        assertEquals(expected.size(), result.size());

        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).getEmail(), result.get(i).getEmail());
            assertEquals(expected.get(i).getPaDispensation(), result.get(i).getPaDispensation());
            assertEquals(expected.get(i).getWpmDispensation(), result.get(i).getWpmDispensation());
        }
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
                .email(EMAIL_CELL_VALUE)
                .build();

        when(rowMock.getCell(anyInt())).thenReturn(defaultCellMock);
        when(defaultCellMock.getNumericCellValue()).thenReturn(DEFAULT_CELL_VALUE);
        when(rowMock.getCell(0)).thenReturn(emailCellMock);
        when(emailCellMock.getStringCellValue()).thenReturn(EMAIL_CELL_VALUE);

        // execute
        var result = dispensationParser.createObjectFromRow(rowMock);

        // verify
        assertNotNull(result);
        assertEquals(expected, result);
        verify(rowMock, times(1)).getCell(0);
        verify(rowMock, times(3)).getCell(anyInt());
    }

    private void setupLookupTable() {
        int i = 0;
        for (var field: DispensationLookupTable.values()) {
            field.setCellNumber(i++);
        }
    }

    /* **************************************************************************************************************
     * Negative tests
     * ************************************************************************************************************** */

    @Test
    void createObjectFromRow_Null() {
        assertThrows(NullPointerException.class, () -> dispensationParser.createObjectFromRow(null));
    }

    @Test
    void createObjectFromRow_Cell_Header_Not_Found() {
        when(rowMock.getCell(anyInt())).thenReturn(defaultCellMock);
        when(rowMock.getCell(-1)).thenReturn(null);
        assertThrows(NullPointerException.class, () -> dispensationParser.createObjectFromRow(rowMock));
        verify(rowMock, times(1)).getCell(-1);
    }

}