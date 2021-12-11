package ch.zhaw.vorwahlen.model.modulestructure;

import ch.zhaw.vorwahlen.model.dto.ElectionStructureDTO;
import ch.zhaw.vorwahlen.model.modules.ElectionSemesters;
import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleCategory;
import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import ch.zhaw.vorwahlen.model.modules.Student;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class generates the structure to be displayed in the frontend.
 */
@RequiredArgsConstructor
public class ModuleStructureGenerator {
    private final List<ModuleStructureElement> electedModuleStructure = new ArrayList<>();
    private final List<ModuleStructureElement> overflowedModuleStructure = new ArrayList<>();
    private final ModuleDefinition moduleDefinition;
    private final Student student;
    private final ModuleElection election;
    private final ElectionSemesters electionSemesters;

    /**
     * Generate the election structure.
     * @return ElectionStructureDTO
     */
    public ElectionStructureDTO generateStructure() {
        var electedModuleList = new ArrayList<>(Set.copyOf(election.getElectedModules()));
        var mappedList = electedModuleList.stream()
                .sorted(Comparator.comparingInt(o -> o.getSemester().getSemester()))
                .map(moduleToModuleStructureElement)
                .toList();
        electedModuleStructure.addAll(mappedList);

        for (ModuleCategory category : ModuleCategory.values()) {
            var moduleDefinitions = cleanModuleDefinitions(moduleDefinition.getDefinitionByCategory(category));
            applyMissingStructureElements(moduleDefinitions, category);
        }
        applyOverflowedModules();
        applyDispensations();
        return new ElectionStructureDTO(electedModuleStructure, overflowedModuleStructure);
    }

    private void applyOverflowedModules() {
        for (ModuleCategory category : ModuleCategory.values()) {
            var moduleDefinitions = cleanModuleDefinitions(moduleDefinition.getDefinitionByCategory(category));
            int totalNumOfAllowedModules = moduleDefinitions.values().stream().reduce(0, Integer::sum);
            var numOfModules = filterAndCountModuleStructureList(electedModuleStructure, mse -> mse.category().equals(category));
            if(numOfModules <= totalNumOfAllowedModules) continue;

            for (int i = 0; i < numOfModules - totalNumOfAllowedModules; i++) {
                var overflowModule = electedModuleStructure.stream()
                        .filter(mse -> category.equals(mse.category()))
                        .findFirst();
                if(overflowModule.isPresent()) {
                    electedModuleStructure.remove(overflowModule.get());
                    overflowedModuleStructure.add(overflowModule.get());
                }
            }
        }
    }

    private Map<Integer, Integer> cleanModuleDefinitions(Map<Integer, Integer> moduleDefinitions) {
        var returnValue = moduleDefinitions;
        if (student.isTZ()) {
            returnValue = moduleDefinitions.entrySet().stream()
                    .filter(entry -> electionSemesters.getSemestersForStudent(student).contains(entry.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        return returnValue;
    }

    private void applyMissingStructureElements(Map<Integer, Integer> moduleDefinitions, ModuleCategory category) {
        int totalNumOfAllowedModules = moduleDefinitions.values().stream().reduce(0, Integer::sum);
        var numOfModules = filterAndCountModuleStructureList(electedModuleStructure, mse -> mse.category().equals(category));
        if(numOfModules == totalNumOfAllowedModules) return;

        var semesterEntryList = new ArrayList<>(moduleDefinitions.entrySet());
        if(semesterEntryList.size() == 1) {
            electedModuleStructure.add(createPlaceholder(category, semesterEntryList.get(0).getKey()));
        }
        generatePlaceholderForMultipleModulesOfCategory(semesterEntryList, moduleDefinitions, category, totalNumOfAllowedModules);
    }

    private Map<Integer, Integer> calculateElectedMap(Map<Integer, Integer> moduleDefinitions, ModuleCategory category) {
        var electedMap = new HashMap<Integer, Integer>();
        moduleDefinitions.keySet().forEach(semester -> {
            var electedNumberOfModuleInSemester = filterAndCountModuleStructureList(electedModuleStructure,
                    mse -> mse.semester() == semester && mse.category().equals(category));
            electedMap.put(semester, electedNumberOfModuleInSemester);
        });
        return electedMap;
    }

    private void generatePlaceholderForMultipleModulesOfCategory(List<Map.Entry<Integer, Integer>> semesterEntryList,
                                                                 Map<Integer, Integer> moduleDefinitions,
                                                                 ModuleCategory category,
                                                                 int totalNumOfAllowedModules) {
        var electedMap = calculateElectedMap(moduleDefinitions, category);
        for (int i = 1; i < semesterEntryList.size(); i++) {
            var semesterEntry1 = semesterEntryList.get(i - 1);
            var semesterEntry2 = semesterEntryList.get(i);

            var electedCount1 = electedMap.get(semesterEntry1.getKey());
            var electedCount2 = electedMap.get(semesterEntry2.getKey());

            var initialBalanceValue = electedCount1 + electedCount2;

            if(electedCount1 < semesterEntry1.getValue() && electedCount2 < semesterEntry2.getValue()) {
                // generate everything
                generatePlaceholder(electedCount1, semesterEntry1, category);
                generatePlaceholder(electedCount2, semesterEntry2, category);
            } else if(electedCount1 < semesterEntry1.getValue()) {
                // balance
                generatePlaceholder(initialBalanceValue, semesterEntry1.getKey(), totalNumOfAllowedModules, category);
            } else if(electedCount2 < semesterEntry2.getValue()) {
                // balance
                generatePlaceholder(initialBalanceValue, semesterEntry2.getKey(), totalNumOfAllowedModules, category);
            }
        }
    }

    private void generatePlaceholder(int initialValue, Map.Entry<Integer, Integer> entry, ModuleCategory category) {
        generatePlaceholder(initialValue, entry.getKey(), entry.getValue(), category);
    }

    private void generatePlaceholder(int initialValue, int key, int max, ModuleCategory category) {
        for (var i = initialValue; i < max; i++) {
            electedModuleStructure.add(createPlaceholder(category, key));
        }
    }

    private Stream<ModuleStructureElement> filterModuleStructureList(List<ModuleStructureElement> structureList, Predicate<ModuleStructureElement> filter) {
        return structureList.stream().filter(filter);
    }

    private int filterAndCountModuleStructureList(List<ModuleStructureElement> structureList, Predicate<ModuleStructureElement> filter) {
        return (int) filterModuleStructureList(structureList, filter).count();
    }

    private final Function<Module, ModuleStructureElement> moduleToModuleStructureElement = module -> {
        var moduleCategory = ModuleCategory.parse(module.getModuleNo(), module.getModuleGroup());
        return new ModuleStructureElement(module.getModuleTitle(),
                module.getModuleNo(),
                false,
                module.getSemester().getSemester(),
                moduleCategory,
                moduleCategory.getCredits());
    };

    private void applyDispensations() {
        dispensateModulesByCategory(student.getWpmDispensation(), ModuleCategory.SUBJECT_MODULE, ModuleCategory.DISPENSED_WPM_MODULE);
        dispensateModulesByCategory(student.getPaDispensation(), ModuleCategory.PROJECT_MODULE, ModuleCategory.DISPENSED_PA_MODULE);
    }

    private void dispensateModulesByCategory(int dispensedCredits, ModuleCategory structureCategory,
                                             ModuleCategory replacementCategory) {
        if(student.isTZ() && !student.isSecondElection()) return;

        while (dispensedCredits > 0) {
            var optionalPlaceholderElement = filterModuleStructureList(electedModuleStructure,
                    mse -> structureCategory.equals(mse.category()) && mse.isPlaceholder()).findFirst();

            if(optionalPlaceholderElement.isPresent()) {
                replaceModuleStructureElementWithDispensation(optionalPlaceholderElement.get(), replacementCategory);
            } else {
                var elementOptional = filterModuleStructureList(electedModuleStructure,
                        mse -> structureCategory.equals(mse.category())).findFirst();

                if (elementOptional.isPresent()) {
                    var moduleStructureElement = replaceModuleStructureElementWithDispensation(elementOptional.get(), replacementCategory);
                    if (ModuleCategory.SUBJECT_MODULE.equals(structureCategory) && !moduleStructureElement.isPlaceholder()) {
                        overflowedModuleStructure.add(moduleStructureElement);
                    }
                }
            }
            dispensedCredits -= replacementCategory.getCredits();
        }
    }

    private ModuleStructureElement replaceModuleStructureElementWithDispensation(ModuleStructureElement moduleStructureElement, ModuleCategory replacementCategory) {
        var index = electedModuleStructure.indexOf(moduleStructureElement);
        electedModuleStructure.remove(moduleStructureElement);
        electedModuleStructure.add(index, createPlaceholder(replacementCategory, moduleStructureElement.semester()));
        return moduleStructureElement;
    }

    private ModuleStructureElement createPlaceholder(ModuleCategory category, int semester) {
        var notAvailableModuleId = "N/A";
        var moduleData = Module.builder().moduleNo(notAvailableModuleId).moduleTitle(category.getDescription()).build();

        return new ModuleStructureElement(moduleData.getModuleTitle(),
                moduleData.getModuleNo(),
                true,
                semester,
                category,
                category.getCredits());
    }

 }
