package ch.zhaw.vorwahlen.mapper;

import ch.zhaw.vorwahlen.model.evento.EventoDataDTO;
import ch.zhaw.vorwahlen.model.evento.EventoData;
import org.springframework.stereotype.Component;

/**
 * Mapping class for {@link EventoData}.
 */
@Component
public class EventoDataMapper implements Mapper<EventoDataDTO, EventoData> {
    @Override
    public EventoDataDTO toDto(EventoData eventoData) {
        return EventoDataDTO.builder()
                .shortDescription(eventoData.getShortDescription())
                .coordinator(eventoData.getCoordinator())
                .learningObjectives(eventoData.getLearningObjectives())
                .moduleContents(eventoData.getModuleContents())
                .literature(eventoData.getLiterature())
                .suppLiterature(eventoData.getSuppLiterature())
                .prerequisites(eventoData.getPrerequisites())
                .moduleStructure(eventoData.getModuleStructure())
                .exams(eventoData.getExams())
                .remarks(eventoData.getRemarks())
                .build();
    }
}
