package ch.zhaw.vorwahlen.model.dto;

import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleElement;

import java.util.List;

public record ModuleStructureDTO(List<ModuleElement> electedModules,
                                 List<Module> overflowedModules) {
}
