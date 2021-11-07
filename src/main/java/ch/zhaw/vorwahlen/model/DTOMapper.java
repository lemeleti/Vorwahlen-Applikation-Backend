package ch.zhaw.vorwahlen.model;

import ch.zhaw.vorwahlen.model.dto.EventoDataDTO;
import ch.zhaw.vorwahlen.model.dto.ModuleDTO;
import ch.zhaw.vorwahlen.model.dto.StudentDTO;
import ch.zhaw.vorwahlen.model.modules.EventoData;
import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.Student;
import ch.zhaw.vorwahlen.service.ClassListService;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@UtilityClass
public class DTOMapper {

    public final Function<Student, StudentDTO> mapStudentToDto = student -> StudentDTO.builder()
            .email(student.getEmail())
            .name(student.getName())
            .clazz(student.getClazz())
            .paDispensation(ClassListService.PA_DISPENSATION)
            .wpmDispensation(ClassListService.WPM_DISPENSATION)
            .isIP(student.isIP())
            .build();

    public final Function<Module, ModuleDTO> mapModuleToDto = module -> ModuleDTO.builder()
            .moduleNo(module.getModuleNo())
            .shortModuleNo(module.getShortModuleNo())
            .moduleTitle(module.getModuleTitle())
            .moduleGroup(module.getModuleGroup())
            .isIPModule(module.isIPModule())
            .institute(module.getInstitute())
            .credits(module.getCredits())
            .language(module.getLanguage())
            .fullTimeSemesterList(parseSemesterListData(module.getFullTimeSemester()))
            .partTimeSemesterList(parseSemesterListData(module.getPartTimeSemester()))
            .build();

    private List<Integer> parseSemesterListData(String data) {
        var stringList = data.split(";");
        return Arrays.stream(stringList)
                .map(Double::parseDouble)
                .map(Double::intValue)
                .toList();
    }

    public final Function<EventoData, EventoDataDTO> mapEventoDataToDto = eventoData -> EventoDataDTO.builder()
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
