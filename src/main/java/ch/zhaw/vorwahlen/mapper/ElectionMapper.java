package ch.zhaw.vorwahlen.mapper;

import ch.zhaw.vorwahlen.model.dto.ModuleElectionDTO;
import ch.zhaw.vorwahlen.model.dto.ValidationSettingDTO;
import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import ch.zhaw.vorwahlen.model.modules.ValidationSetting;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ElectionMapper implements Mapper<ModuleElectionDTO, ModuleElection> {
    private final Mapper<ValidationSettingDTO, ValidationSetting> mapper;

    @Override
    public ModuleElectionDTO toDto(ModuleElection moduleElection) {
        return ModuleElectionDTO
                .builder()
                .isElectionValid(moduleElection.isElectionValid())
                .electedModules(modulesToModuleNos(moduleElection.getElectedModules()))
                .validationSettingDTO(mapper.toDto(moduleElection.getValidationSetting()))
                .build();

    }

    private Set<String> modulesToModuleNos(Set<Module> modules) {
        return modules
                .stream()
                .map(Module::getModuleNo)
                .collect(Collectors.toSet());
    }
}
