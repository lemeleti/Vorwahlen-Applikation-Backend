package ch.zhaw.vorwahlen.model.modulestructure;

import ch.zhaw.vorwahlen.model.modules.ModuleCategory;

public record ModuleStructureElement(String name,
                                     String id,
                                     boolean isPlaceholder,
                                     int semester,
                                     ModuleCategory category,
                                     int credits) {}