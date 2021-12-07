package ch.zhaw.vorwahlen.model.modulestructure;

import ch.zhaw.vorwahlen.model.dto.ElectionStructureDTO;
import ch.zhaw.vorwahlen.model.modules.ElectionSemesters;
import ch.zhaw.vorwahlen.model.modules.ExecutionSemester;
import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleCategory;
import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import ch.zhaw.vorwahlen.model.modules.Student;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ModuleStructureGenerator {
    private final List<ModuleStructureElement> electedModuleStructure = new ArrayList<>();
    private final List<ModuleStructureElement> overflowedModuleStructure = new ArrayList<>();
    private final ModuleDefinition moduleDefinition;
    private final Student student;
    private final ModuleElection election;
    private final ElectionSemesters electionSemesters;
    private List<Module> electedModuleList;
    private boolean hasElectedModules;

    public ElectionStructureDTO generateStructure() {
        electedModuleList = new ArrayList<>(Set.copyOf(election.getElectedModules()));
        electedModuleList.sort(Comparator.comparingInt(o -> o.getSemester().getSemester()));
        hasElectedModules = !election.getElectedModules().isEmpty();
        for (ModuleCategory category : ModuleCategory.values()) {
            generateModuleElements(moduleDefinition.getDefinitionByCategory(category), category);
        }
        generateOverflowedModules();
        applyDispensations();
        return new ElectionStructureDTO(electedModuleStructure, overflowedModuleStructure);
    }

    private void applyDispensations() {
        dispensateModulesByCategory(student.getWpmDispensation(), ModuleCategory.SUBJECT_MODULE, ModuleCategory.DISPENSED_WPM_MODULE);
        dispensateModulesByCategory(student.getPaDispensation(), ModuleCategory.PROJECT_MODULE, ModuleCategory.DISPENSED_PA_MODULE);
    }

    private void dispensateModulesByCategory(int dispensedCredits, ModuleCategory structureCategory,
                                                ModuleCategory replacementCategory) {
        if(student.isTZ() && !student.isSecondElection()) return;

        while (dispensedCredits > 0) {
            var elementOptional = electedModuleStructure
                    .stream()
                    .filter(element -> structureCategory.equals(element.category()))
                    .findFirst();

            if (elementOptional.isPresent()) {
                var paModule = elementOptional.get();
                var index = electedModuleStructure.indexOf(paModule);
                electedModuleStructure.remove(paModule);
                electedModuleStructure.add(index, createStructureElement(null, replacementCategory, paModule.semester()));
            }

            dispensedCredits -= replacementCategory.getCredits();
        }
    }

    private void generateOverflowedModules() {
        if (electedModuleList == null) return;

        var iterator = electedModuleList.iterator();
        while (iterator.hasNext()) {
            var module = iterator.next();
            iterator.remove();
            var semester = module.getSemester().getSemester();
            var category = ModuleCategory.parse(module.getModuleNo(), module.getModuleGroup());
            overflowedModuleStructure.add(createStructureElement(module, category, semester));
        }
    }

    private void generateModuleElements(Map<Integer, Integer> modules, ModuleCategory category) {
        if (student.isTZ()) {
            modules = modules.entrySet().stream()
                            .filter(entry -> electionSemesters.getSemestersForStudent(student).contains(entry.getKey()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        for (var entry : modules.entrySet()) {
            for (var i = 0; i < entry.getValue(); i++) {
                var semester = entry.getKey();
                var module = findModuleByCategory(category);
                var element = createStructureElement(module, category, semester);
                electedModuleStructure.add(element);
            }
        }
    }

    private Module findModuleByCategory(ModuleCategory category) {
        Module module = null;
        Predicate<Module> hasModuleForCategory = m ->
                (category.equals(ModuleCategory.parse(m.getModuleNo(), m.getModuleGroup())));

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

    private ModuleStructureElement createStructureElement(Module module, ModuleCategory category, int semester) {
        var notAvailableModuleId = "N/A";
        var moduleData = Optional.ofNullable(module)
                .orElse(Module.builder().moduleNo(notAvailableModuleId).moduleTitle(category.getDescription()).build());

        if (module != null && !ExecutionSemester.AUTUMN_AND_SPRING.equals(module.getSemester())) {
            semester = module.getSemester().getSemester();
        }

        return new ModuleStructureElement(
                moduleData.getModuleTitle(), moduleData.getModuleNo(),
                module == null, semester, category, category.getCredits());
    }
 }
