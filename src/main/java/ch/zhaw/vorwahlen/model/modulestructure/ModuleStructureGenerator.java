package ch.zhaw.vorwahlen.model.modulestructure;

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
    private boolean hasElectedModules;

    public Map<String, List<?>> generateStructure() {
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

        structureMap.put("electedModules", structure);
        structureMap.put("overflowedModules", overflowedElectedModules);

        return structureMap;
    }

    private void generateModuleElements(Map<Integer, Integer> modules, ModuleCategory category) {
        for (Map.Entry<Integer, Integer> entry : modules.entrySet()) {
            for (var i = 0; i < entry.getValue(); i++) {
                var semester = entry.getKey();
                Optional<Module> moduleOptional = Optional.ofNullable(findModuleByCategory(category, semester));
                ModuleElement element;

                if (hasElectedModules && moduleOptional.isPresent()) {
                    element = createModule(moduleOptional.get(), category, semester);
                } else {
                    element = createFillerModule(category, semester);
                }
                structure.add(element);
            }
        }
    }

    private int getCreditsForCategory(ModuleCategory category) {
        return switch (category) {
            case SUBJECT_MODULE, INTERDISCIPLINARY_MODULE -> 4;
            case CONTEXT_MODULE -> 2;
            case PROJECT_MODULE -> 6;
            case BACHELOR_MODULE -> 12;
        };
    }

    private String getNameForCategory(ModuleCategory category) {
        return switch (category) {
            case CONTEXT_MODULE -> "Kontext Wahlpflichmodul";
            case SUBJECT_MODULE -> "Fachliches Wahlpflichmodul";
            case INTERDISCIPLINARY_MODULE -> "Überfachliches Wahlpflichmodul";
            case PROJECT_MODULE -> "Projektarbeit in der Informatik";
            case BACHELOR_MODULE -> "Bachelorarbeit in der Informatik";
        };
    }

    private Module findModuleByCategory(ModuleCategory category, int semester) {
        Module module = null;
        Predicate<Module> hasModuleForCategory = m ->
                ModuleCategory.parse(m.getModuleNo(), m.getModuleGroup()).equals(category) &&
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

    private ModuleElement createFillerModule(ModuleCategory category, int semester) {
        return new ModuleElement(
                null,
                getNameForCategory(category),
                "N/A",
                true,
                semester,
                category,
                getCreditsForCategory(category)
        );
    }

    private ModuleElement createModule(Module electedModule, ModuleCategory category, int semester) {
        return new ModuleElement(null,
                electedModule.getModuleTitle(),
                electedModule.getModuleNo(),
                false,
                semester,
                category,
                electedModule.getCredits());
    }
}
