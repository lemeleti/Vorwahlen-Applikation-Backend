package ch.zhaw.vorwahlen.parser;

import ch.zhaw.vorwahlen.model.modules.Student;
import ch.zhaw.vorwahlen.model.modules.StudentLookupTable;
import org.apache.poi.ss.usermodel.Row;

import java.io.InputStream;

public class ClassListParser extends ExcelParser<Student, StudentLookupTable> {

    public static final String TZ_CLASS_REGEX = "[A-Za-z]+\\d{2}t[a-z]+_[A-Za-z]+";

    /**
     * Create instance.
     * @param fileLocation where the Excel file is found.
     * @param workSheet which should be parsed.
     */
    public ClassListParser(InputStream fileLocation, String workSheet) {
        super(fileLocation, workSheet, StudentLookupTable.class);
    }

    @Override
    Student createObjectFromRow(Row row) {
        var studentBuilder = Student.builder();
        for(var field: StudentLookupTable.values()) {
            var cellValue = row.getCell(field.getCellNumber()).toString();
            switch (field) {
                case EMAIL -> studentBuilder.email(cellValue);
                case NAME -> studentBuilder.name(cellValue);
                case CLAZZ -> {
                    studentBuilder.clazz(cellValue);
                    studentBuilder.isTZ(cellValue.matches(TZ_CLASS_REGEX));
                }
            }
        }
        return studentBuilder.build();
    }

}
