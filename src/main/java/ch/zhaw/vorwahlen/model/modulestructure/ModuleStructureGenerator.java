package ch.zhaw.vorwahlen.model.modulestructure;

import ch.zhaw.vorwahlen.model.dto.ElectionStructureDTO;
import ch.zhaw.vorwahlen.model.modules.ElectionSemesters;
import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleCategory;
import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import ch.zhaw.vorwahlen.model.modules.Student;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

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
        //todo für Teilzeit nur im zweiten Durchlauf.
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
            var semester = calculateSemester(module); // todo use real semester
            var category = ModuleCategory.parse(module.getModuleNo(), module.getModuleGroup());
            overflowedModuleStructure.add(createStructureElement(module, category, semester));
        }
    }

    private void generateModuleElements(Map<Integer, Integer> modules, ModuleCategory category) {
        if (student.isTZ()) {
            var electionSemesters = this.electionSemesters.getSemestersForStudent(student);
            electionSemesters.forEach(modules::remove);
        }

        for (var entry : modules.entrySet()) {
            for (var i = 0; i < entry.getValue(); i++) {
                var semester = entry.getKey();
                var module = findModuleByCategory(category, semester);
                var element = createStructureElement(module, category, semester);
                electedModuleStructure.add(element);
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
        return module.getFullTimeSemester().contains(String.valueOf(compareSemester));
    }

    private int calculateSemester(Module module) {
        var executionSemesters = module.getFullTimeSemester().split(";");
        var semester = (int) Float.parseFloat(executionSemesters[0]);
        if (student.isTZ() && student.isSecondElection()) {
            semester = (int) Float.parseFloat(executionSemesters[executionSemesters.length - 1]);
        }

        return semester;
    }

    private ModuleStructureElement createStructureElement(Module module, ModuleCategory category, int semester) {
        var notAvailableModuleId = "N/A";
        var moduleData = Optional.ofNullable(module)
                .orElse(Module.builder().moduleNo(notAvailableModuleId).moduleTitle(category.getDescription()).build());

        return new ModuleStructureElement(
                moduleData.getModuleTitle(), moduleData.getModuleNo(),
                module == null, semester, category, category.getCredits());
    }
 }
