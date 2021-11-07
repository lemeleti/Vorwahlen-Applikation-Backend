package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.model.dto.EventoDataDTO;
import ch.zhaw.vorwahlen.model.dto.ModuleDTO;
import ch.zhaw.vorwahlen.model.dto.ModuleElectionDTO;
import ch.zhaw.vorwahlen.model.dto.StudentDTO;
import ch.zhaw.vorwahlen.model.modules.EventoData;
import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import ch.zhaw.vorwahlen.model.modules.Student;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@UtilityClass
public class DTOMapper {

    public static final String SEMESTER_LIST_DELIMITER = ";";


    final Function<ModuleElection, ModuleElectionDTO> mapElectionToDto = election -> ModuleElectionDTO.builder()
            .isElectionValid(election.isElectionValid())
            .electedModules(mapModuleSetToModuleNo(election.getElectedModules()))
            .overflowedElectedModules(mapModuleSetToModuleNo(election.getOverflowedElectedModules()))
            .build();

    private Set<String> mapModuleSetToModuleNo(Set<Module> moduleSet) {
        return moduleSet.stream().map(Module::getModuleNo).collect(Collectors.toSet());
    }

    final Function<Student, StudentDTO> mapStudentToDto = student -> StudentDTO.builder()
            .email(student.getEmail())
            .name(student.getName())
            .clazz(student.getClazz())
            .paDispensation(ClassListService.PA_DISPENSATION)
            .wpmDispensation(ClassListService.WPM_DISPENSATION)
            .isIP(student.isIP())
            .build();

    final Function<Module, ModuleDTO> mapModuleToDto = module -> ModuleDTO.builder()
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

    public final Function<ModuleDTO, Module> mapDtoToModule = moduleDTO -> Module.builder()
            .moduleNo(moduleDTO.getModuleNo())
            .shortModuleNo(moduleDTO.getShortModuleNo())
            .moduleTitle(moduleDTO.getModuleTitle())
            .moduleGroup(moduleDTO.getModuleGroup())
            .isIPModule(moduleDTO.isIPModule())
            .institute(moduleDTO.getInstitute())
            .credits(moduleDTO.getCredits())
            .language(moduleDTO.getLanguage())
            .fullTimeSemester(parseSemesterListToString(moduleDTO.getFullTimeSemesterList()))
            .partTimeSemester(parseSemesterListToString(moduleDTO.getPartTimeSemesterList()))
            .build();

    private String parseSemesterListToString(List<Integer> list) {
        return list.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(SEMESTER_LIST_DELIMITER));
    }

    private List<Integer> parseSemesterListData(String data) {
        var stringList = data.split(SEMESTER_LIST_DELIMITER);
        return Arrays.stream(stringList)
                .map(Double::parseDouble)
                .map(Double::intValue)
                .toList();
    }

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
