package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.model.dto.EventoDataDTO;
import ch.zhaw.vorwahlen.model.modules.EventoData;
import lombok.experimental.UtilityClass;

import java.util.function.Function;

@UtilityClass
public class DTOMapper {

    final Function<EventoData, EventoDataDTO> mapEventoDataToDto = eventoData -> EventoDataDTO.builder()
            .moduleStructure(eventoData.getModuleStructure())
            .learningObjectives(eventoData.getLearningObjectives())
            .shortDescription(eventoData.getShortDescription())
            .suppLiterature(eventoData.getSuppLiterature())
            .coordinator(eventoData.getCoordinator())
            .exams(eventoData.getExams())
            .literature(eventoData.getLiterature())
            .moduleContents(eventoData.getModuleContents())
            .prerequisites(eventoData.getPrerequisites())
            .remarks(eventoData.getRemarks())
            .build();

}
