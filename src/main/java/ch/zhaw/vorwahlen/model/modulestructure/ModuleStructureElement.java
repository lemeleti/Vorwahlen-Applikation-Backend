package ch.zhaw.vorwahlen.model.modulestructure;

import ch.zhaw.vorwahlen.model.core.module.ModuleCategory;

/**
 * Model for the module structure element.
 */
public record ModuleStructureElement(String name,
                                     String id,
                                     boolean isPlaceholder,
                                     int semester,
                                     ModuleCategory category,
                                     int credits) {}