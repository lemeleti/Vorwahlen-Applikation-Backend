package ch.zhaw.vorwahlen.model.modulestructure;

import ch.zhaw.vorwahlen.model.dto.StudentDTO;
import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleCategory;
import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class ModuleStructureGenerator {
    private final ModuleStructure moduleStructure;
    private final List<ModuleElement> structure = new ArrayList<>();
    private List<Module> electedModules;
    private List<Module> overflowedElectedModules;

    public List<ModuleElement> generateStructure(StudentDTO student, ModuleElection election) {
        generateFillerModule(moduleStructure.contextModules(), ModuleCategory.CONTEXT_MODULE);
        generateFillerModule(moduleStructure.projectModule(), ModuleCategory.PROJECT_MODULE);
        generateFillerModule(moduleStructure.subjectModules(), ModuleCategory.SUBJECT_MODULE);
        generateFillerModule(moduleStructure.interdisciplinaryModules(), ModuleCategory.INTERDISCIPLINARY_MODULE);
        generateFillerModule(moduleStructure.bachelorModule(), ModuleCategory.BACHELOR_MODULE);

        electedModules = election.getElectedModules().stream().toList();
        overflowedElectedModules = election.getOverflowedElectedModules().stream().toList();
        return structure;
    }

    private void generateFillerModule(Map<Integer, Integer> modules, ModuleCategory category) {
        Module electedModule;
        for (Map.Entry<Integer, Integer> entry : modules.entrySet()) {
            for (var i = 0; i < entry.getValue(); i++) {
                electedModule = findModuleByCategory(category);
                ModuleElement element;

                if (electedModule == null) {
                    element = new ModuleElement(
                            null,
                            getNameForCategory(category),
                            "N/A",
                            true,
                            entry.getKey(),
                            category,
                            getCreditsForCategory(category)
                    );
                } else {
                    var semester = Integer.parseInt(electedModule.getFullTimeSemester()); //todo teilzeit abfangen
                    element = new ModuleElement(null,
                            electedModule.getModuleTitle(),
                            electedModule.getModuleNo(),
                            false,
                            semester, // todo
                            category,
                            electedModule.getCredits());
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
            case INTERDISCIPLINARY_MODULE -> "Ueberfachliches Wahlpflichmodul";
            case PROJECT_MODULE -> "Projektarbeit in der Informatik";
            case BACHELOR_MODULE -> "Bachelorarbeit in der Informatik";
        };
    }

    private Module findModuleByCategory(ModuleCategory category) {
        Module module = null;
        Optional<Module> moduleOptional = electedModules.stream()
                .filter(m -> ModuleCategory.parse(m.getModuleNo(), m.getModuleGroup()).equals(category))
                .findFirst();
        if (moduleOptional.isPresent()) {
            module = moduleOptional.get();
            electedModules.remove(module);
        }

        return module;
    }
}
