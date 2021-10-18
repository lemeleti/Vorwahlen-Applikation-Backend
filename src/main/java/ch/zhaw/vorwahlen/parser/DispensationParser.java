package ch.zhaw.vorwahlen.parser;

import ch.zhaw.vorwahlen.model.modules.DispensationLookupTable;
import ch.zhaw.vorwahlen.model.modules.Student;
import org.apache.poi.ss.usermodel.Row;

import java.io.InputStream;

public class DispensationParser extends ExcelParser<Student, DispensationLookupTable> {

    /**
     * Create instance.
     * @param fileLocation where the Excel file is found.
     * @param workSheet which should be parsed.
     */
    public DispensationParser(InputStream fileLocation, String workSheet) {
        super(fileLocation, workSheet, DispensationLookupTable.class);
    }

    @Override
    Student createObjectFromRow(Row row) {
        var studentBuilder = Student.builder();
        for(var field: DispensationLookupTable.values()) {
            var cell = row.getCell(field.getCellNumber());
            switch (field) {
                case EMAIL -> studentBuilder.email(cell.getStringCellValue());
                case PA -> studentBuilder.paDispensation((int) cell.getNumericCellValue());
                case WPM -> studentBuilder.wpmDispensation((int) cell.getNumericCellValue());
            }
        }
        return studentBuilder.build();
    }

}
