package ch.zhaw.vorwahlen.model.modulestructure;

import ch.zhaw.vorwahlen.model.dto.ModuleStructureDTO;
import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleCategory;
import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import ch.zhaw.vorwahlen.model.modules.Student;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class ModuleStructureGenerator {
    private final ModuleStructure moduleStructure;
    private final List<ModuleStructureElement> structure = new ArrayList<>();
    private final ModuleElection election;
    private final List<Module> electedModules = new ArrayList<>();
    private final List<Module> overflowedElectedModules = new ArrayList<>();
    private final Student student;
    private boolean hasElectedModules;

    public ModuleStructureDTO generateStructure() {
        if (election != null) {
            electedModules.addAll(election.getElectedModules());
            overflowedElectedModules.addAll(election.getOverflowedElectedModules());
            hasElectedModules = true;
        }

        generateModuleElements(moduleStructure.contextModules(), ModuleCategory.CONTEXT_MODULE);
        generateModuleElements(moduleStructure.projectModule(), ModuleCategory.PROJECT_MODULE);
        generateModuleElements(moduleStructure.subjectModules(), ModuleCategory.SUBJECT_MODULE);
        generateModuleElements(moduleStructure.interdisciplinaryModules(), ModuleCategory.INTERDISCIPLINARY_MODULE);
        generateModuleElements(moduleStructure.bachelorModule(), ModuleCategory.BACHELOR_MODULE);

        return new ModuleStructureDTO(structure, overflowedElectedModules);
    }

    private void generateModuleElements(Map<Integer, Integer> modules, ModuleCategory category) {
        var paDispensationCredits = student.getPaDispensation();
        var wpmDispensationCredits = student.getWpmDispensation();
        var backup = category;
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
                structure.add(element);
                category = backup;
            }
        }
    }

    private Module findModuleByCategory(ModuleCategory category, int semester) {
        Module module = null;
        Predicate<Module> hasModuleForCategory = m ->
                category.equals(ModuleCategory.parse(m.getModuleNo(), m.getModuleGroup())) &&
                (int) Float.parseFloat(m.getFullTimeSemester()) == semester;

        if (hasElectedModules) {
            var moduleOptional = electedModules.stream()
                    .filter(hasModuleForCategory)
                    .findFirst();
            if (moduleOptional.isPresent()) {
                module = moduleOptional.get();
                electedModules.remove(module);
            }
        }
        return module;
    }

    private ModuleStructureElement createStructureElement(Module module,
                                                          ModuleCategory category, int semester) {

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
