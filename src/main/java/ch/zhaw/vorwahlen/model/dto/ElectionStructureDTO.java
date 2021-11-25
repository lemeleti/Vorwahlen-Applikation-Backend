package ch.zhaw.vorwahlen.model.dto;

import ch.zhaw.vorwahlen.model.modulestructure.ModuleStructureElement;

import java.util.List;

public record ElectionStructureDTO(List<ModuleStructureElement> electedModules,
                                   List<ModuleStructureElement> overflowedModules) {
}
