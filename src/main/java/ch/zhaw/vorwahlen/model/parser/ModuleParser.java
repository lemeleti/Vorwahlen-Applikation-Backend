package ch.zhaw.vorwahlen.model.parser;

import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleStringTable;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static ch.zhaw.vorwahlen.model.modules.ModuleStringTable.*;

public class ModuleParser {
    public static List<Module> parseModulesFromXLSX(String location) throws IOException {
        List<Module> moduleList = new ArrayList<>();
        Module module;

        try (FileInputStream fis = new FileInputStream(location)) {
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            // todo somehow let the user decide which sheet should be parsed.
            XSSFSheet moduleSheet = workbook.getSheet("Module 2025");

            Iterator<Row> rowIterator = moduleSheet.rowIterator();
            if (rowIterator.hasNext())
                setCellIndexes(rowIterator.next());

            while (rowIterator.hasNext()) {
                if ((module = createModuleFromRow(rowIterator.next())) != null)
                    moduleList.add(module);
            }

        }
        return moduleList;
    }

    private static void setCellIndexes (Row row) {
        for (Cell cell : row) {
            String cellValue = cell.getStringCellValue().trim().replace("\n", "");
            ModuleStringTable stringTable = ModuleStringTable.getConstantByValue(cellValue);
            if (stringTable != null)
                stringTable.setCellNumber(cell.getColumnIndex());
        }
    }

    private static Module createModuleFromRow(Row row) {
        Module module = null;
        Cell moduleGroupCell = row.getCell(GROUP.getCellNumber());
        if (row.getCell(0) != null && moduleGroupCell != null &&
                (moduleGroupCell.toString().contains("IT5") || moduleGroupCell.toString().contains("IT6"))) {

            Module.ModuleBuilder builder = Module.builder();
            for (ModuleStringTable field : ModuleStringTable.values()) {
                setModuleField(builder, field, row.getCell(field.getCellNumber()).toString());
            }

            module = builder.build();
        }
        return module;
    }

    private static void setModuleField(Module.ModuleBuilder builder, ModuleStringTable stringTable, String data) {
        switch (stringTable) {
            case NO -> builder.moduleNo(data);
            case SHORT_NO -> builder.shortModuleNo(data);
            case TITLE -> builder.moduleTitle(data);
            case ID -> builder.moduleId((int) Double.parseDouble(data));
            case GROUP -> builder.moduleGroup(data);
            case IP -> builder.isIPModule(data.contains("x"));
            case INSTITUTE -> builder.institute(data);
            case CREDITS -> builder.credits((byte) Double.parseDouble(data));
            case LANGUAGE -> builder.language(data);
        }
    }
}
