package ch.zhaw.vorwahlen.model.modulestructure;

import ch.zhaw.vorwahlen.model.modules.ModuleCategory;

public record ModuleStructureElement(String moduleName,
                                     String moduleId,
                                     boolean isFiller,
                                     int semester,
                                     ModuleCategory moduleCategory,
                                     int credits) {}