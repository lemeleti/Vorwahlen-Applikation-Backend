package ch.zhaw.vorwahlen.mapper;

import ch.zhaw.vorwahlen.model.dto.ModuleDTO;
import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleCategory;
import org.springframework.stereotype.Component;

@Component
public class ModuleMapper implements Mapper<ModuleDTO, Module> {
    @Override
    public ModuleDTO toDto(Module module) {
        //todo getConsecutiveModuleNo should not be nullable
        return ModuleDTO.builder()
                .moduleNo(module.getModuleNo())
                .shortModuleNo(module.getShortModuleNo())
                .moduleTitle(module.getModuleTitle())
                .moduleId(module.getModuleId())
                .moduleGroup(module.getModuleGroup())
                .institute(module.getInstitute())
                .category(ModuleCategory.parse(module.getModuleNo(), module.getModuleGroup()))
                .credits(module.getCredits())
                .language(module.getLanguage())
                .semester(module.getSemester().getSemester())
                .consecutiveModuleNo(module.getConsecutiveModuleNo() == null ? "" : module.getConsecutiveModuleNo())
                .build();

    }
}
