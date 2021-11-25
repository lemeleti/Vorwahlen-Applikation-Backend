package ch.zhaw.vorwahlen.exporter;

import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleCategory;
import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Export all module elections in the desired output format provided by @fame.
 */
public class ExcelModuleElectionExporter implements ModuleElectionExporter {
    private static final String[] HEADER_DATA = {"E-Mail", "Name", "In welcher Klasse sind Sie?",
            "Konsekutive Wahlpflichmodule", "Wahlpflichmodule", "Wahlmodule"};
    private static final String DELIMITER = "; ";

    @Override
    public byte[] export(List<ModuleElection> electionList) {
        try (var workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet("Modulvorwahlen");
            writeHeaderToSheet(sheet);
            writeElectionsToSheet(electionList, sheet);

            try (var os = new ByteArrayOutputStream()) {
                workbook.write(os);
                return os.toByteArray();
            }
        } catch (IOException e) {
            // todo use custom exception
            throw new RuntimeException(e.getMessage());
        }
    }

    private void writeHeaderToSheet(XSSFSheet sheet) {
        var cellIndex = 0;
        var headerRow = sheet.createRow(0);
        for (String data : HEADER_DATA) {
            headerRow.createCell(cellIndex++).setCellValue(data);
        }
    }

    private void writeElectionsToSheet(List<ModuleElection> electionList, XSSFSheet sheet) {
        var rowCounter = 1;
        for (ModuleElection moduleElection : electionList) {
            var row = sheet.createRow(rowCounter++);
            var cellCounter = 0;
            var electionData = transformModuleElectionToData(moduleElection);

            for (var data : electionData) {
                row.createCell(cellCounter++).setCellValue(data);
            }
        }
    }

    private String[] transformModuleElectionToData(ModuleElection election) {
        var student = election.getStudent();
        var modules = election.getElectedModules().stream().toList();
        Predicate<Module> isConsecutiveModule = m -> m.getConsecutiveModuleNo() != null &&
                !m.getConsecutiveModuleNo().isBlank();
        Predicate<Module> isSubjectModule = m -> m.getConsecutiveModuleNo() == null &&
                ModuleCategory.SUBJECT_MODULE.equals(ModuleCategory.parse(m.getModuleNo(), m.getModuleGroup()));
        Predicate<Module> isContextModule = m ->
                ModuleCategory.CONTEXT_MODULE.equals(ModuleCategory.parse(m.getModuleNo(), m.getModuleGroup()));

        return new String[] {
                student.getEmail(),
                student.getName(),
                student.getStudentClass().getName(),
                getModulesAsStringForPredicate(modules, isConsecutiveModule),
                getModulesAsStringForPredicate(modules, isSubjectModule),
                getModulesAsStringForPredicate(modules, isContextModule)
        };
    }

    private String getModulesAsStringForPredicate(List<Module> modules, Predicate<Module> predicate) {
        return modules
                .stream()
                .filter(predicate)
                .map(Module::getModuleTitle)
                .collect(Collectors.joining(DELIMITER));
    }
}
