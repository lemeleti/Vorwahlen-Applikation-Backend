package ch.zhaw.vorwahlen.mapper;

import ch.zhaw.vorwahlen.model.dto.ModuleDTO;
import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleCategory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ModuleMapper implements Mapper<ModuleDTO, Module> {
    @Override
    public ModuleDTO toDto(Module module) {
        //todo getConsecutiveModuleNo should not be nullable
        return ModuleDTO.builder()
                .moduleNo(module.getModuleNo())
                .moduleTitle(module.getModuleTitle())
                .category(ModuleCategory.parse(module.getModuleNo(), module.getModuleGroup()))
                .credits(module.getCredits())
                .language(module.getLanguage())
                .executionSemester(toExecutionSemester(module))
                .consecutiveModuleNo(module.getConsecutiveModuleNo() == null ? "" : module.getConsecutiveModuleNo())
                .build();

    }

    private ModuleDTO.ExecutionSemester toExecutionSemester(Module module) {
        var fullTimeSemesterList = executionSemestersToIntegers(module.getFullTimeSemester());
        var partTimeSemesterList = executionSemestersToIntegers(module.getPartTimeSemester());
        return new ModuleDTO.ExecutionSemester(fullTimeSemesterList, partTimeSemesterList);
    }

    private List<Integer> executionSemestersToIntegers(String executionSemesters) {
        var delimiter = ";";
        return Arrays
                .stream(executionSemesters.split(delimiter))
                .map(s -> (int) Float.parseFloat(s))
                .toList();
    }
}
