package ch.zhaw.vorwahlen.model.modulestructure;

import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleCategory;

public class ModuleElementFactory {
    public ModuleStructureElement createModuleElement(Module module, ModuleCategory category, int semester) {
        ModuleStructureElement element;
        if (module == null) {
            element = createFillerModule(category, semester);
        } else {
            element = createModule(module, category, semester);
        }
        return element;
    }

    private ModuleStructureElement createFillerModule(ModuleCategory category, int semester) {
        return new ModuleStructureElement(
                category.getDescription(),
                "N/A",
                true,
                semester,
                category,
                category.getCredits()
        );
    }

    private ModuleStructureElement createModule(Module electedModule, ModuleCategory category, int semester) {
        return new ModuleStructureElement(
                electedModule.getModuleTitle(),
                electedModule.getModuleNo(),
                false,
                semester,
                category,
                electedModule.getCredits());
    }
}
