package ch.zhaw.vorwahlen.model.modulestructure;

import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleCategory;

public class ModuleElementFactory {
    public ModuleElement createModuleElement(Module module, ModuleCategory category, int semester) {
        ModuleElement element;
        if (isModuleDispensed(category, module)) {
            element = createDispensedModule(category, semester);
        } else if (module == null) {
            element = createFillerModule(category, semester);
        } else {
            element = createModule(module, category, semester);
        }
        return element;
    }

    private ModuleElement createDispensedModule(ModuleCategory category, int semester) {
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

    private boolean isModuleDispensed(ModuleCategory category, Module module) {
        return (ModuleCategory.DISPENSED_PA_MODULE.equals(category) ||
                ModuleCategory.DISPENSED_WPM_MODULE.equals(category)) && module == null;
    }

    private int getCreditsForCategory(ModuleCategory category) {
        return switch (category) {
            case SUBJECT_MODULE, INTERDISCIPLINARY_MODULE, DISPENSED_WPM_MODULE -> 4;
            case CONTEXT_MODULE -> 2;
            case PROJECT_MODULE, DISPENSED_PA_MODULE -> 6;
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
            case DISPENSED_PA_MODULE -> "Projektarbeit in der Informatik - Dispensiert";
            case DISPENSED_WPM_MODULE -> "Fachliches Wahlpflichmodul - Dispensiert";
        };
    }

}
