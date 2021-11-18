package ch.zhaw.vorwahlen.model.dto;

import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleStructureElement;

import java.util.List;

public record ModuleStructureDTO(List<ModuleStructureElement> electedModules,
                                 List<Module> overflowedModules) {
}
