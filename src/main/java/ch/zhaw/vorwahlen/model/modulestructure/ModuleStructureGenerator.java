package ch.zhaw.vorwahlen.model.modulestructure;

import ch.zhaw.vorwahlen.model.dto.ElectionStructureDTO;
import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleCategory;
import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import ch.zhaw.vorwahlen.model.modules.Student;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class ModuleStructureGenerator {
    private final List<ModuleStructureElement> electedModuleStructure = new ArrayList<>();
    private final List<ModuleStructureElement> overflowedModuleStructure = new ArrayList<>();
    private final ModuleDefinition moduleDefinition;
    private final Student student;
    private final ModuleElection election;
    private List<Module> electedModuleList;
    private boolean hasElectedModules;

    public ElectionStructureDTO generateStructure() {
        electedModuleList = new ArrayList<>(Set.copyOf(election.getElectedModules()));
        hasElectedModules = !election.getElectedModules().isEmpty();

        generateModuleElements(moduleDefinition.contextModules(), ModuleCategory.CONTEXT_MODULE);
        generateModuleElements(moduleDefinition.projectModule(), ModuleCategory.PROJECT_MODULE);
        generateModuleElements(moduleDefinition.subjectModules(), ModuleCategory.SUBJECT_MODULE);
        generateModuleElements(moduleDefinition.interdisciplinaryModules(), ModuleCategory.INTERDISCIPLINARY_MODULE);
        generateModuleElements(moduleDefinition.bachelorModule(), ModuleCategory.BACHELOR_MODULE);

        if (electedModuleList != null) {
            while (!electedModuleList.isEmpty()) {
                var module = electedModuleList.remove(0);
                var semester = calculateSemester(module); // todo use real semester
                var category = ModuleCategory.parse(module.getModuleNo(), module.getModuleGroup());
                overflowedModuleStructure.add(createStructureElement(module, category, semester));
            }
        }

        return new ElectionStructureDTO(electedModuleStructure, overflowedModuleStructure);
    }

    private void generateModuleElements(Map<Integer, Integer> modules, ModuleCategory category) {
        var paDispensationCredits = student.getPaDispensation();
        var wpmDispensationCredits = student.getWpmDispensation();
        var backup = category;

        if (student.isTZ() && student.isSecondElection()) {
            modules.remove(5);
            modules.remove(6);
        } else if (student.isTZ()) {
            modules.remove(7);
            modules.remove(8);
        }

        for (Map.Entry<Integer, Integer> entry : modules.entrySet()) {
            if (ModuleCategory.PROJECT_MODULE.equals(category) && paDispensationCredits > 0) {
                category = ModuleCategory.DISPENSED_PA_MODULE;
                paDispensationCredits = 0;
            }

            for (var i = 0; i < entry.getValue(); i++) {
                var semester = entry.getKey();

                if (ModuleCategory.SUBJECT_MODULE.equals(category) && wpmDispensationCredits > 0) {
                    category = ModuleCategory.DISPENSED_WPM_MODULE;
                    wpmDispensationCredits -= ModuleCategory.DISPENSED_WPM_MODULE.getCredits();
                }
                var module = findModuleByCategory(category, semester);
                var element = createStructureElement(module, category, semester);
                electedModuleStructure.add(element);
                category = backup;
            }
        }
    }

    private Module findModuleByCategory(ModuleCategory category, int semester) {
        Module module = null;
        Predicate<Module> hasModuleForCategory = m ->
                category.equals(ModuleCategory.parse(m.getModuleNo(), m.getModuleGroup())) &&
                        hasModuleForSemester(m, semester);

        if (hasElectedModules) {
            var moduleOptional = electedModuleList.stream()
                    .filter(hasModuleForCategory)
                    .findFirst();
            if (moduleOptional.isPresent()) {
                module = moduleOptional.get();
                electedModuleList.remove(module);
            }
        }
        return module;
    }

    private boolean hasModuleForSemester(Module module, int compareSemester) {
        var executionSemestersStr = module.getFullTimeSemester();
        if (student.isTZ()) {
            executionSemestersStr = module.getPartTimeSemester();
        }

        return executionSemestersStr.contains(String.valueOf(compareSemester));
    }

    private int calculateSemester(Module module) {
        var executionSemesterStr = student.isTZ() ? module.getPartTimeSemester() : module.getFullTimeSemester();
        var executionSemesters = executionSemesterStr.split(";");
        var semester = (int) Float.parseFloat(executionSemesters[0]);
        if (student.isTZ() && student.isSecondElection()) {
            semester = (int) Float.parseFloat(executionSemesters[executionSemesters.length - 1]);
        }

        return semester;
    }

    private ModuleStructureElement createStructureElement(Module module, ModuleCategory category, int semester) {

        var name = category.getDescription();
        var moduleId = "N/A";
        var isFiller = true;

        if (module != null) {
            name = module.getModuleTitle();
            moduleId = module.getModuleNo();
            isFiller = false;
        }

        return new ModuleStructureElement(
                name,
                moduleId,
                isFiller,
                semester,
                category,
                category.getCredits()
        );
    }
 }
