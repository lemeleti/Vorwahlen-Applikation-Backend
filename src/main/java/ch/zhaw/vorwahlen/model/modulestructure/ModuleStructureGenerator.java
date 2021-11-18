package ch.zhaw.vorwahlen.model.modulestructure;

import ch.zhaw.vorwahlen.model.dto.ModuleStructureDTO;
import ch.zhaw.vorwahlen.model.dto.StudentDTO;
import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleCategory;
import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class ModuleStructureGenerator {
    private final ModuleStructure moduleStructure;
    private final List<ModuleElement> structure = new ArrayList<>();
    private final ModuleElection election;
    private final List<Module> electedModules = new ArrayList<>();
    private final List<Module> overflowedElectedModules = new ArrayList<>();
    private final StudentDTO student; //todo replace with Student class
    private boolean hasElectedModules;

    public ModuleStructureDTO generateStructure() {
        Map<String, List<?>> structureMap = new HashMap<>();
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
        ModuleElementFactory factory = new ModuleElementFactory();
        var paDispensationCredits = student.getPaDispensation();
        var wpmDispensationCredits = student.getWpmDispensation();
        ModuleCategory backup = category;
        for (Map.Entry<Integer, Integer> entry : modules.entrySet()) {
            if (ModuleCategory.PROJECT_MODULE.equals(category) && paDispensationCredits > 0) {
                category = ModuleCategory.DISPENSED_PA_MODULE;
                paDispensationCredits = 0;
            }

            for (var i = 0; i < entry.getValue(); i++) {
                var semester = entry.getKey();

                if (ModuleCategory.SUBJECT_MODULE.equals(category) && wpmDispensationCredits > 0) {
                    category = ModuleCategory.DISPENSED_WPM_MODULE;
                    wpmDispensationCredits -= 4;
                }

                structure.add(factory.createModuleElement(findModuleByCategory(category, semester), category, semester));
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
            Optional<Module> moduleOptional = electedModules.stream()
                    .filter(hasModuleForCategory)
                    .findFirst();
            if (moduleOptional.isPresent()) {
                module = moduleOptional.get();
                electedModules.remove(module);
            }
        }
        return module;
    }
}
