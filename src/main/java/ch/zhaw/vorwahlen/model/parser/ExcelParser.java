package ch.zhaw.vorwahlen.model.parser;

import ch.zhaw.vorwahlen.model.modules.StringTable;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract excel parser for type <T>
 * @param <T> type to be parsed from the provided excel sheet
 * @param <S> lookup table from type enum
 */
@RequiredArgsConstructor
public abstract class ExcelParser<T, S extends StringTable<?>> {
    private final String fileLocation;
    private final String workSheet;
    private final Class<S> clazz;

    /**
     * Parse all modules from the provided Excel sheet
     * @return list of <T>
     * @throws IOException if file not found or file not an Excel sheet
     */
    public List<T> parseModulesFromXLSX() throws IOException {
        List<T> moduleList = new ArrayList<>();
        T object;

        try (var fis = new FileInputStream(fileLocation)) {
            var workbook = new XSSFWorkbook(fis);
            var moduleSheet = workbook.getSheet(workSheet);

            var rowIterator = moduleSheet.rowIterator();
            if (rowIterator.hasNext()) {
                setCellIndexes(rowIterator.next());
            }

            while (rowIterator.hasNext()) {
                if ((object = createObjectFromRow(rowIterator.next())) != null) {
                    moduleList.add(object);
                }
            }

        }
        return moduleList;
    }

    /**
     * Set all columns in lookup table where the constant enum value is found.
     * @param row Excel sheet row
     */
    void setCellIndexes(Row row) {
        for (var cell : row) {
            var cellValue = cell.getStringCellValue().trim().replace("\n", "");

            S stringTable = StringTable.getConstantByValue(cellValue, clazz);
            if (stringTable != null) {
                stringTable.setCellNumber(cell.getColumnIndex());
            }
        }
    }

    /**
     * Create <T> from the excel sheet row
     * @param row excel sheet row
     * @return <T>
     */
    abstract T createObjectFromRow(Row row);
}
