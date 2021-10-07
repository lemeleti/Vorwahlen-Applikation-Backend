package ch.zhaw.vorwahlen.parser;

import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleStringTable;
import org.apache.poi.ss.usermodel.Row;

import static ch.zhaw.vorwahlen.model.modules.ModuleStringTable.GROUP;

/**
 * Concrete module excel parser.
 */
public class ModuleParser extends ExcelParser<Module, ModuleStringTable> {

    /**
     * Create instance.
     * @param fileLocation where the excel file is found.
     * @param workSheet which should be parsed.
     */
    public ModuleParser(String fileLocation, String workSheet) {
        super(fileLocation, workSheet, ModuleStringTable.class);
    }

    @Override
    Module createObjectFromRow(Row row) {
        Module module = null;
        var moduleGroupCell = row.getCell(GROUP.getCellNumber());
        if (row.getCell(0) != null && moduleGroupCell != null &&
                (moduleGroupCell.toString().contains("IT5") || moduleGroupCell.toString().contains("IT6"))) {

            var builder = Module.builder();
            for (var field : ModuleStringTable.values()) {
                setModuleField(builder, field, row.getCell(field.getCellNumber()).toString());
            }

            module = builder.build();
        }
        return module;
    }

    private void setModuleField(Module.ModuleBuilder builder, ModuleStringTable stringTable, String data) {
        switch (stringTable) {
            case NO -> builder.moduleNo(data);
            case SHORT_NO -> builder.shortModuleNo(data);
            case TITLE -> builder.moduleTitle(data);
            case ID -> builder.moduleId((int) Double.parseDouble(data));
            case GROUP -> builder.moduleGroup(data);
            case IP -> builder.isIPModule(data.contains("x"));
            case INSTITUTE -> builder.institute(data.toUpperCase());
            case CREDITS -> builder.credits((byte) Double.parseDouble(data));
            case LANGUAGE -> builder.language(data);
        }
    }
}
