package ch.zhaw.vorwahlen.mapper;

import ch.zhaw.vorwahlen.config.ResourceBundleMessageLoader;
import ch.zhaw.vorwahlen.constants.ResourceMessageConstants;
import ch.zhaw.vorwahlen.exception.StudentNotFoundException;
import ch.zhaw.vorwahlen.model.dto.ModuleElectionDTO;
import ch.zhaw.vorwahlen.model.dto.ValidationSettingDTO;
import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import ch.zhaw.vorwahlen.model.modules.Student;
import ch.zhaw.vorwahlen.model.modules.ValidationSetting;
import ch.zhaw.vorwahlen.repository.ModuleRepository;
import ch.zhaw.vorwahlen.repository.StudentRepository;
import ch.zhaw.vorwahlen.repository.ValidationSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ElectionMapper implements Mapper<ModuleElectionDTO, ModuleElection> {
    private final Mapper<ValidationSettingDTO, ValidationSetting> mapper;
    private final ModuleRepository moduleRepository;
    private final StudentRepository studentRepository;
    private final ValidationSettingRepository validationSettingRepository;

    @Override
    public ModuleElectionDTO toDto(ModuleElection moduleElection) {
        return ModuleElectionDTO
                .builder()
                .id(moduleElection.getId())
                .studentEmail(moduleElection.getStudent().getEmail())
                .isElectionValid(moduleElection.isElectionValid())
                .electedModules(modulesToModuleNos(moduleElection.getElectedModules()))
                .validationSettingDTO(mapper.toDto(moduleElection.getValidationSetting()))
                .build();

    }

    @Override
    public ModuleElection toInstance(ModuleElectionDTO moduleElectionDTO) {
        var moduleElection = new ModuleElection();
        var modules = moduleRepository.findAllById(moduleElectionDTO.getElectedModules());
        var student = fetchStudentById(moduleElectionDTO.getStudentEmail());
        var validationSettingDto = moduleElectionDTO.getValidationSettingDTO();
        var validationSetting = validationSettingDto == null
                                                                        ? new ValidationSetting()
                                                                        : fetchValidationSetting(student.getEmail());

        moduleElection.setElectedModules(new HashSet<>(modules));
        moduleElection.setElectionValid(moduleElectionDTO.isElectionValid());
        moduleElection.setStudent(student);
        moduleElection.setValidationSetting(validationSetting);

        return moduleElection;
    }

    private Set<String> modulesToModuleNos(Set<Module> modules) {
        return modules
                .stream()
                .map(Module::getModuleNo)
                .collect(Collectors.toSet());
    }

    private ValidationSetting fetchValidationSetting(String id) {
        // todo: replace with exception instead of or else
        return validationSettingRepository.findValidationSettingByStudentMail(id).orElse(new ValidationSetting());
    }

    private Student fetchStudentById(String id) {
        return studentRepository.findById(id).orElseThrow(() -> {
            var formatString =
                    ResourceBundleMessageLoader.getMessage(ResourceMessageConstants.ERROR_STUDENT_NOT_FOUND);
            return new StudentNotFoundException(String.format(formatString, id));
        });
    }


}
