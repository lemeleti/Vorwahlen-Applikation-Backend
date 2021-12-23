package ch.zhaw.vorwahlen.parser;

import ch.zhaw.vorwahlen.model.ExecutionSemester;
import ch.zhaw.vorwahlen.model.core.module.Module;
import ch.zhaw.vorwahlen.parser.lookup.ModuleLookupTable;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.InputStream;

import static ch.zhaw.vorwahlen.parser.lookup.ModuleLookupTable.GROUP;

/**
 * Concrete module Excel parser.
 */
public class ModuleParser extends ExcelParser<Module, ModuleLookupTable> {

    public static final String MODULE_GROUP_IT_5 = "IT5";
    public static final String MODULE_GROUP_IT_6 = "IT6";

    /**
     * Create instance.
     * @param fileLocation where the Excel file is found.
     * @param workSheet which should be parsed.
     */
    public ModuleParser(InputStream fileLocation, String workSheet) {
        super(fileLocation, workSheet, ModuleLookupTable.class);
    }

    @Override
    Module createObjectFromRow(Row row) {
        Module module = null;
        var moduleGroupCell = row.getCell(GROUP.getCellNumber());
        if (moduleGroupCell != null && belongsToWantedModuleGroup(moduleGroupCell)) {
            var builder = Module.builder();
            for (var field : ModuleLookupTable.values()) {
                setModuleField(builder, field, row.getCell(field.getCellNumber()).toString().trim());
            }
            module = builder.build();
        }
        return module;
    }

    private boolean belongsToWantedModuleGroup(Cell moduleGroupCell) {
        var value = moduleGroupCell.toString();
        return value.contains(MODULE_GROUP_IT_5) || value.contains(MODULE_GROUP_IT_6);
    }

    private void setModuleField(Module.ModuleBuilder builder, ModuleLookupTable field, String data) {
        switch (field) {
            case NO -> builder.moduleNo(data);
            case SHORT_NO -> builder.shortModuleNo(data);
            case TITLE -> builder.moduleTitle(data);
            case ID -> builder.moduleId((int) Double.parseDouble(data));
            case GROUP -> builder.moduleGroup(data);
            case INSTITUTE -> builder.institute(data.toUpperCase());
            case CREDITS -> builder.credits((byte) Double.parseDouble(data));
            case LANGUAGE -> builder.language(data);
            case SEMESTER_FULL_TIME -> builder.semester(ExecutionSemester.parseFromString(data));
        }
    }

}
