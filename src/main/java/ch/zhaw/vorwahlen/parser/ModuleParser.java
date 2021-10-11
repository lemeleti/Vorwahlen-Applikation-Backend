package ch.zhaw.vorwahlen.parser;

import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleLookupTable;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import static ch.zhaw.vorwahlen.model.modules.ModuleLookupTable.GROUP;

/**
 * Concrete module Excel parser.
 */
public class ModuleParser extends ExcelParser<Module, ModuleLookupTable> {

    /**
     * Create instance.
     * @param fileLocation where the Excel file is found.
     * @param workSheet which should be parsed.
     */
    public ModuleParser(String fileLocation, String workSheet) {
        super(fileLocation, workSheet, ModuleLookupTable.class);
    }

    @Override
    Module createObjectFromRow(Row row) {
        Module module = null;
        var moduleGroupCell = row.getCell(GROUP.getCellNumber());
        if (moduleGroupCell != null && belongsToWantedModuleGroup(moduleGroupCell)) {
            var builder = Module.builder();
            for (var field : ModuleLookupTable.values()) {
                setModuleField(builder, field, row.getCell(field.getCellNumber()).toString());
            }
            module = builder.build();
        }
        return module;
    }

    private boolean belongsToWantedModuleGroup(Cell moduleGroupCell) {
        var value = moduleGroupCell.toString();
        return value.contains("IT5") || value.contains("IT6");
    }

    private void setModuleField(Module.ModuleBuilder builder, ModuleLookupTable stringTable, String data) {
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
