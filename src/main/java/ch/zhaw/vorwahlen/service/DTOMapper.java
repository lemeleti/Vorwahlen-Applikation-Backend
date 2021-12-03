package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.model.dto.*;
import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.*;
import lombok.experimental.UtilityClass;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@UtilityClass
public class DTOMapper {

    public ElectionStatusDTO mapModuleElectionStatusToDto(ModuleElectionStatus status) {
        return new ElectionStatusDTO(mapElectionStatusElementToDto(status.getSubjectValidation()),
                                     mapElectionStatusElementToDto(status.getContextValidation()),
                                     mapElectionStatusElementToDto(status.getInterdisciplinaryValidation()),
                                     mapElectionStatusElementToDto(status.getAdditionalValidation()));
    }

    private ElectionStatusElementDTO mapElectionStatusElementToDto(ModuleElectionStatus.ModuleElectionStatusElement status){
        return new ElectionStatusElementDTO(status.getModuleCategory(),
                                            status.isValid(),
                                            status.getReasons());
    }

    final Function<ModuleElection, ModuleElectionDTO> mapElectionToDto = election -> ModuleElectionDTO.builder()
            .isElectionValid(election.isElectionValid())
            .electedModules(mapModuleSetToModuleNo(election.getElectedModules()))
            .validationSettingDTO(mapValidationSettingToDto(election.getValidationSetting()))
            .build();

    private ValidationSettingDTO mapValidationSettingToDto(ValidationSetting setting) {
        return new ValidationSettingDTO(setting.isRepetent(),
                                        setting.hadAlreadyElectedTwoConsecutiveModules(),
                                        setting.isSkipConsecutiveModuleCheck());
    }

    private Set<String> mapModuleSetToModuleNo(Set<Module> moduleSet) {
        return moduleSet.stream().map(Module::getModuleNo).collect(Collectors.toSet());
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
