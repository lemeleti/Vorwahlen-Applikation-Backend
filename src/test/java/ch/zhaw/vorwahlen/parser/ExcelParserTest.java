package ch.zhaw.vorwahlen.parser;

import ch.zhaw.vorwahlen.dummy.DummyLookupTable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExcelParserTest {

    private static final String MODULE_LIST_FILE_NAME = "Liste_alle_Module_SM2025_SGL_Def_1.7-2021-03-29.xlsx";
    private static final String WORK_SHEET_NAME = "Module 2025";

    ExcelParser<?, DummyLookupTable> parser;

    @BeforeEach
    void setUp() {
        var fis = getClass().getClassLoader().getResourceAsStream(MODULE_LIST_FILE_NAME);
        parser = mock(ExcelParser.class, Mockito.withSettings()
                    .useConstructor(fis, WORK_SHEET_NAME, DummyLookupTable.class)
                    .defaultAnswer(CALLS_REAL_METHODS));
    }

    @AfterEach
    void tearDown() {
        for (var field: DummyLookupTable.values()) {
            field.setCellNumber(-1);
        }
    }

    /* **************************************************************************************************************
     * Positive tests
     * ************************************************************************************************************** */

    @Test
    void testParseModulesFromXLSX() {
        when(parser.createObjectFromRow(any())).thenReturn(null);

        var result = assertDoesNotThrow(() -> parser.parseModulesFromXLSX());

        assertNotNull(result);
        assertEquals(0, result.size());
        assertEquals(10, DummyLookupTable.NO.getCellNumber());
        assertEquals(0, DummyLookupTable.SHORT_NO.getCellNumber());
        assertEquals(11, DummyLookupTable.TITLE.getCellNumber());
        assertEquals(-1, DummyLookupTable.NO_COLUMN.getCellNumber());
    }

    @Test
    void testParseModulesFromXLSX_No_Header_Row() {
        var fis = getClass().getClassLoader()
                .getResourceAsStream("No_Header_" + MODULE_LIST_FILE_NAME);

        parser = mock(ExcelParser.class, Mockito.withSettings()
                .useConstructor(fis, WORK_SHEET_NAME, DummyLookupTable.class)
                .defaultAnswer(CALLS_REAL_METHODS));
        when(parser.createObjectFromRow(any())).thenReturn(null);

        var result = assertDoesNotThrow(() -> parser.parseModulesFromXLSX());

        assertNotNull(result);
        assertEquals(0, result.size());
        for(var field: DummyLookupTable.values()) {
            assertEquals(-1, field.getCellNumber());
        }
    }

    /* **************************************************************************************************************
     * Negative tests
     * ************************************************************************************************************** */

    @Test
    void testParseModulesFromXLSX_FileLocation_Null() {
        parser = mock(ExcelParser.class, Mockito.withSettings()
                .useConstructor(null, WORK_SHEET_NAME, DummyLookupTable.class)
                .defaultAnswer(CALLS_REAL_METHODS));
        assertThrows(NullPointerException.class, () -> parser.parseModulesFromXLSX());
    }

    @Test
    void testParseModulesFromXLSX_Worksheet_Null() {
        var fis = getClass().getClassLoader().getResourceAsStream(MODULE_LIST_FILE_NAME);
        parser = mock(ExcelParser.class, Mockito.withSettings()
                .useConstructor(fis, null, DummyLookupTable.class)
                .defaultAnswer(CALLS_REAL_METHODS));
        assertThrows(NullPointerException.class, () -> parser.parseModulesFromXLSX());
    }

    @Test
    void testParseModulesFromXLSX_LookupTable_Null() {
        var fis = getClass().getClassLoader().getResourceAsStream(MODULE_LIST_FILE_NAME);
        parser = mock(ExcelParser.class, Mockito.withSettings()
                .useConstructor(fis, WORK_SHEET_NAME, null)
                .defaultAnswer(CALLS_REAL_METHODS));
        assertThrows(NullPointerException.class, () -> parser.parseModulesFromXLSX());
    }

    @Test
    void testParseModulesFromXLSX_FileNotFound() {
        var fis = getClass().getClassLoader().getResourceAsStream("nonexisting file.xlsx");
        parser = mock(ExcelParser.class, Mockito.withSettings()
                .useConstructor(fis, WORK_SHEET_NAME, DummyLookupTable.class)
                .defaultAnswer(CALLS_REAL_METHODS));
        assertThrows(NullPointerException.class, () -> parser.parseModulesFromXLSX());
    }

    @Test
    void testParseModulesFromXLSX_NonExistingWorksheet() {
        var fis = getClass().getClassLoader().getResourceAsStream(MODULE_LIST_FILE_NAME);
        parser = mock(ExcelParser.class, Mockito.withSettings()
                .useConstructor(fis, "Inkognito", DummyLookupTable.class)
                .defaultAnswer(CALLS_REAL_METHODS));
        assertThrows(NullPointerException.class, () -> parser.parseModulesFromXLSX());
    }

}