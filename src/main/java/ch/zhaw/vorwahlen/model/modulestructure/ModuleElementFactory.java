package ch.zhaw.vorwahlen.model.modulestructure;

import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleCategory;

public class ModuleElementFactory {
    public ModuleElement createModuleElement(Module module, ModuleCategory category, int semester) {
        ModuleElement element;
        if (module == null) {
            element = createFillerModule(category, semester);
        } else {
            element = createModule(module, category, semester);
        }
        return element;
    }

    private ModuleElement createFillerModule(ModuleCategory category, int semester) {
        return new ModuleElement(
                null,
                category.getDescription(),
                "N/A",
                true,
                semester,
                category,
                category.getCredits()
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
