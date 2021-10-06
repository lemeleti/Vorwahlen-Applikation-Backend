package ch.zhaw.vorwahlen.model.parser;

import ch.zhaw.vorwahlen.model.modules.StringTable;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@AllArgsConstructor
public abstract class ExcelParser<T, S extends StringTable<?>> {
    private final String fileLocation;
    private final String workSheet;
    private final Class<S> clazz;

    public List<T> parseModulesFromXLSX() throws IOException {
        List<T> moduleList = new ArrayList<>();
        T object;

        try (FileInputStream fis = new FileInputStream(fileLocation)) {
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            XSSFSheet moduleSheet = workbook.getSheet(workSheet);

            Iterator<Row> rowIterator = moduleSheet.rowIterator();
            if (rowIterator.hasNext())
                setCellIndexes(rowIterator.next());

            while (rowIterator.hasNext()) {
                if ((object = createObjectFromRow(rowIterator.next())) != null)
                    moduleList.add(object);
            }

        }
        return moduleList;
    }

    void setCellIndexes(Row row) {
        for (Cell cell : row) {
            String cellValue = cell.getStringCellValue().trim().replace("\n", "");

            S stringTable = StringTable.getConstantByValue(cellValue, clazz);
            if (stringTable != null)
                stringTable.setCellNumber(cell.getColumnIndex());
        }
    }

    abstract T createObjectFromRow(Row row);
}
