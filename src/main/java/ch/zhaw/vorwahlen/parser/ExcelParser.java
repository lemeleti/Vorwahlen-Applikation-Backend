package ch.zhaw.vorwahlen.parser;

import ch.zhaw.vorwahlen.model.modules.parser.LookupTable;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract excel parser for type {@code <T>}.<br/>
 *
 * To use this class you need a model class representing {@code <T>} which holds the desired data.<br/>
 * To find out on which cells the desired fields are we use a lookup table {@code <S>}.<br/>
 * This lookup table is an Enum which has to contain the header string and the cell number (default: -1).<br/>
 * With this string the parser can scan the Excel sheet and set the cell number (which column the header was found).<br/>
 *
 * @param <T> type to be parsed from the provided Excel sheet.
 * @param <S> lookup table from type enum.
 */
@RequiredArgsConstructor
@Log
public abstract class ExcelParser<T, S extends LookupTable<?>> {

    private final InputStream fileInputStream;
    private final String workSheet;
    private final Class<S> lookupTableClass;

    /**
     * Parse all modules from the provided Excel sheet.
     * @return list of {@code} <T>}.
     * @throws IOException if file not found or file not an Excel sheet.
     */
    public List<T> parseModulesFromXLSX() throws IOException {
        var moduleList = new ArrayList<T>();
        T object;

        try (var fis = fileInputStream) {
            var workbook = new XSSFWorkbook(fis);
            var moduleSheet = workbook.getSheet(workSheet);

            var rowIterator = moduleSheet.rowIterator();
            if (rowIterator.hasNext()) {
                setCellNumbersInLookupTableFromHeaderRow(rowIterator.next());
            }

            while (rowIterator.hasNext()) {
                if ((object = createObjectFromRow(rowIterator.next())) != null) {
                    moduleList.add(object);
                }
            }

        }
        return moduleList;
    }

    private void setCellNumbersInLookupTableFromHeaderRow(Row row) {
        for (var cell : row) {
            try {
                var cellValue = cell.getStringCellValue().trim().replace("\n", "");
                var lookupTable = LookupTable.getConstantByValue(cellValue, lookupTableClass);
                if (lookupTable != null) {
                    lookupTable.setCellNumber(cell.getColumnIndex());
                }
            } catch (IllegalStateException e) {
                log.warning(e.getMessage());
            }
        }
    }

    /**
     * Create <T> from the Excel sheet row.
     * @param row Excel sheet row.
     * @return <T>
     */
    abstract T createObjectFromRow(Row row);
}
