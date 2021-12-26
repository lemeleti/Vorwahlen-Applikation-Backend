package ch.zhaw.vorwahlen.exporter;

import ch.zhaw.vorwahlen.config.ResourceBundleMessageLoader;
import ch.zhaw.vorwahlen.constants.ResourceMessageConstants;
import ch.zhaw.vorwahlen.exception.ExportException;
import ch.zhaw.vorwahlen.model.core.module.Module;
import ch.zhaw.vorwahlen.model.core.module.ModuleCategory;
import ch.zhaw.vorwahlen.model.core.election.Election;
import lombok.extern.java.Log;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Export all elections in the desired output format provided by @fame.
 */
@Log
public class ExcelElectionExporter implements ElectionExporter {
    private static final String[] HEADER_DATA = {"E-Mail", "Name", "In welcher Klasse sind Sie?",
            "Konsekutive Wahlpflichmodule", "Wahlpflichmodule", "Wahlmodule"};
    private static final String DELIMITER = "; ";

    @Override
    public byte[] export(Set<Election> electionSet) {
        try (var workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet("Modulvorwahlen");
            writeHeaderToSheet(sheet);
            writeElectionsToSheet(electionSet, sheet);

            try (var os = new ByteArrayOutputStream()) {
                workbook.write(os);
                return os.toByteArray();
            }
        } catch (IOException e) {
            throw new ExportException(ResourceBundleMessageLoader.getMessage(ResourceMessageConstants.ERROR_EXPORT_EXCEPTION), e);
        }
    }

    private void writeHeaderToSheet(XSSFSheet sheet) {
        var cellIndex = 0;
        var headerRow = sheet.createRow(0);
        for (String data : HEADER_DATA) {
            headerRow.createCell(cellIndex++).setCellValue(data);
        }
    }

    private void writeElectionsToSheet(Set<Election> electionSet, XSSFSheet sheet) {
        var rowCounter = 1;
        for (var election : electionSet) {
            var row = sheet.createRow(rowCounter++);
            var cellCounter = 0;
            var electionData = transformElectionToData(election);

            for (var data : electionData) {
                row.createCell(cellCounter++).setCellValue(data);
            }
        }
    }

    private String[] transformElectionToData(Election election) {
        var student = election.getStudent();
        var modules = election.getElectedModules().stream().toList();
        Predicate<Module> isConsecutiveModule = m -> m.getConsecutiveModuleNo() != null &&
                !m.getConsecutiveModuleNo().isBlank();
        Predicate<Module> isSubjectModule = m -> (m.getConsecutiveModuleNo() == null &&
                ModuleCategory.SUBJECT_MODULE.equals(ModuleCategory.parse(m.getModuleNo(), m.getModuleGroup()))) ||
                (m.getConsecutiveModuleNo() == null &&
                        ModuleCategory.INTERDISCIPLINARY_MODULE.equals(ModuleCategory.parse(m.getModuleNo(), m.getModuleGroup())));
        Predicate<Module> isContextModule = m ->
                ModuleCategory.CONTEXT_MODULE.equals(ModuleCategory.parse(m.getModuleNo(), m.getModuleGroup()));

        return new String[] {
                student.getEmail(),
                student.getName(),
                student.getStudentClass().getName(),
                getModulesAsString(modules, mapConsecutiveModule, isConsecutiveModule),
                getModulesAsString(modules, mapModule, isSubjectModule),
                getModulesAsString(modules, mapModule, isContextModule)
        };
    }

    private String getModulesAsString(List<Module> modules,
                                                  Function<Module, String> map,
                                                  Predicate<Module> predicate) {
        return modules
                .stream()
                .filter(predicate)
                .map(map)
                .collect(Collectors.joining(DELIMITER));
    }

    private final Function<Module, String> mapConsecutiveModule = module -> {
        var moduleTitle = module.getModuleTitle();
        if ("Englisch".equals(module.getLanguage())) {
            moduleTitle = moduleTitle.concat(" (E)");
        }

        return moduleTitle.concat(" ".concat(module.getShortModuleNo()));
    };

    private final Function<Module, String> mapModule = module ->
            mapConsecutiveModule.apply(module).concat(" ").concat(module.getSemester().getDescription());
}
