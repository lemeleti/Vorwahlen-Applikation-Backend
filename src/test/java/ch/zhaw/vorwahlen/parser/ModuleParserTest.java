package ch.zhaw.vorwahlen.parser;

import ch.zhaw.vorwahlen.model.ExecutionSemester;
import ch.zhaw.vorwahlen.model.core.module.Module;
import ch.zhaw.vorwahlen.parser.lookup.ModuleLookupTable;
import ch.zhaw.vorwahlen.repository.ModuleRepository;
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
class ModuleParserTest {

    private static final String MODULE_LIST_FILE_NAME = "Liste_alle_Module_SM2025_SGL_Def_1.7-2021-03-29.xlsx";
    private static final String WORK_SHEET_NAME = "Module 2025";
    private static final String DEFAULT_CELL_VALUE = "0";
    private static final String MODULE_GROUPS_CONTAINS_IT5 = "ET5,IT5,ST5";
    private static final String MODULE_GROUPS_CONTAINS_IT6 = "ET5,IT6,ST5";
    private static final String MODULE_GROUPS_CONTAINS_NOT_IT5_OR_IT6= "ET5,ST5";
    private static final int GROUP_CELL_NUMBER = 4;
    private static final String INVALID_CELL_VALUE_FOR_NON_TEXT = "a";

    ModuleParser moduleParser;

    @Autowired
    ModuleRepository moduleRepository;

    @Mock Row rowMock;
    @Mock Cell defaultCellMock;
    @Mock Cell groupCellMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        moduleParser = new ModuleParser(getClass().getClassLoader().getResourceAsStream(MODULE_LIST_FILE_NAME), WORK_SHEET_NAME);
    }

    @AfterEach
    void tearDown() {
        for (var field: ModuleLookupTable.values()) {
            field.setCellNumber(-1);
        }
    }

    /* **************************************************************************************************************
     * Positive tests
     * ************************************************************************************************************** */

    @Test
    @Sql("classpath:sql/modules.sql")
    void parseModulesFromXLSX() throws IOException {
        // prepare
        var expected = sortByModuleNo(moduleRepository.findAll());

        // execute
        var result = sortByModuleNo(moduleParser.parseFromXLSX());

        // verify
        assertNotNull(result);
        assertEquals(expected.size(), result.size());
        assertIterableEquals(expected, result);
    }

    private List<Module> sortByModuleNo(List<Module> list) {
        return list.stream()
                .sorted(Comparator.comparing(Module::getModuleNo))
                .toList();
    }

    @Test
    void createObjectFromRow_IT5() {
        // prepare
        setupLookupTable();

        var expectedModule = Module.builder()
                .moduleId(Integer.parseInt(DEFAULT_CELL_VALUE))
                .moduleGroup(MODULE_GROUPS_CONTAINS_IT5)
                .moduleNo(DEFAULT_CELL_VALUE)
                .moduleTitle(DEFAULT_CELL_VALUE)
                .shortModuleNo(DEFAULT_CELL_VALUE)
                .credits(Byte.parseByte(DEFAULT_CELL_VALUE))
                .institute(DEFAULT_CELL_VALUE)
                .language(DEFAULT_CELL_VALUE)
                .semester(ExecutionSemester.SPRING)
                .build();

        when(rowMock.getCell(anyInt())).thenReturn(defaultCellMock);
        when(defaultCellMock.toString()).thenReturn(DEFAULT_CELL_VALUE);

        when(rowMock.getCell(GROUP_CELL_NUMBER)).thenReturn(groupCellMock);
        when(groupCellMock.toString()).thenReturn(MODULE_GROUPS_CONTAINS_IT5);

        // execute
        var resultModule = moduleParser.createObjectFromRow(rowMock);

        // verify
        assertNotNull(resultModule);
        assertEquals(expectedModule, resultModule);
        verify(rowMock, times(10)).getCell(anyInt());
        verify(rowMock, times(2)).getCell(GROUP_CELL_NUMBER);
    }

    @Test
    void createObjectFromRow_IT6() {
        // prepare
        setupLookupTable();

        var expectedModule = Module.builder()
                .moduleId(Integer.parseInt(DEFAULT_CELL_VALUE))
                .moduleGroup(MODULE_GROUPS_CONTAINS_IT6)
                .moduleNo(DEFAULT_CELL_VALUE)
                .moduleTitle(DEFAULT_CELL_VALUE)
                .shortModuleNo(DEFAULT_CELL_VALUE)
                .credits(Byte.parseByte(DEFAULT_CELL_VALUE))
                .institute(DEFAULT_CELL_VALUE)
                .language(DEFAULT_CELL_VALUE)
                .semester(ExecutionSemester.SPRING)
                .build();

        when(rowMock.getCell(anyInt())).thenReturn(defaultCellMock);
        when(defaultCellMock.toString()).thenReturn(DEFAULT_CELL_VALUE);

        when(rowMock.getCell(GROUP_CELL_NUMBER)).thenReturn(groupCellMock);
        when(groupCellMock.toString()).thenReturn(MODULE_GROUPS_CONTAINS_IT6);

        // execute
        var resultModule = moduleParser.createObjectFromRow(rowMock);

        // verify
        assertNotNull(resultModule);
        assertEquals(expectedModule, resultModule);
        verify(rowMock, times(10)).getCell(anyInt());
        verify(rowMock, times(2)).getCell(GROUP_CELL_NUMBER);
    }

    @Test
    void createObjectFromRow_Not_IT5_Or_IT6() {
        // prepare
        when(rowMock.getCell(GROUP_CELL_NUMBER)).thenReturn(groupCellMock);
        when(groupCellMock.toString()).thenReturn(MODULE_GROUPS_CONTAINS_NOT_IT5_OR_IT6);

        // execute
        var resultModuleFilteredGroup = moduleParser.createObjectFromRow(rowMock);

        // verify
        assertNull(resultModuleFilteredGroup);
    }

    private void setupLookupTable() {
        int i = 0;
        for (var field: ModuleLookupTable.values()) {
            field.setCellNumber(i++);
        }
    }

    /* **************************************************************************************************************
     * Negative tests
     * ************************************************************************************************************** */

    @Test
    void createObjectFromRow_Null() {
        assertThrows(NullPointerException.class, () -> moduleParser.createObjectFromRow(null));
    }

    @Test
    void createObjectFromRow_Group_Not_Found() {
        when(rowMock.getCell(anyInt())).thenReturn(groupCellMock);
        assertNull(moduleParser.createObjectFromRow(rowMock));
        verify(rowMock, times(1)).getCell(-1);
    }

    @Test
    void createObjectFromRow_Parsing_Invalid_Type() {
        setupLookupTable();

        when(rowMock.getCell(anyInt())).thenReturn(defaultCellMock);
        when(defaultCellMock.toString()).thenReturn(INVALID_CELL_VALUE_FOR_NON_TEXT);

        when(rowMock.getCell(GROUP_CELL_NUMBER)).thenReturn(groupCellMock);
        when(groupCellMock.toString()).thenReturn(MODULE_GROUPS_CONTAINS_IT5);

        assertThrows(NumberFormatException.class, () -> moduleParser.createObjectFromRow(rowMock));
    }
}