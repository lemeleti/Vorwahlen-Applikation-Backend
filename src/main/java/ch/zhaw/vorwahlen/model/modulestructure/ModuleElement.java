package ch.zhaw.vorwahlen.model.modulestructure;

import ch.zhaw.vorwahlen.model.modules.ModuleCategory;

public record ModuleElement(Object next, String moduleName, String moduleId, boolean isFiller, int semester,
                            ModuleCategory moduleCategory, int credits) {
}