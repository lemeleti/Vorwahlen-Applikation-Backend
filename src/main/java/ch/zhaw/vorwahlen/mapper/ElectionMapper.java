package ch.zhaw.vorwahlen.mapper;

import ch.zhaw.vorwahlen.config.ResourceBundleMessageLoader;
import ch.zhaw.vorwahlen.constants.ResourceMessageConstants;
import ch.zhaw.vorwahlen.exception.StudentNotFoundException;
import ch.zhaw.vorwahlen.exception.ValidationSettingNotFoundException;
import ch.zhaw.vorwahlen.model.core.election.ElectionDTO;
import ch.zhaw.vorwahlen.model.core.validationsetting.ValidationSettingDTO;
import ch.zhaw.vorwahlen.model.core.module.Module;
import ch.zhaw.vorwahlen.model.core.election.Election;
import ch.zhaw.vorwahlen.model.core.student.Student;
import ch.zhaw.vorwahlen.model.core.validationsetting.ValidationSetting;
import ch.zhaw.vorwahlen.repository.ModuleRepository;
import ch.zhaw.vorwahlen.repository.StudentRepository;
import ch.zhaw.vorwahlen.repository.ValidationSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapping class for {@link Election}.
 */
@Component
@RequiredArgsConstructor
public class ElectionMapper implements Mapper<ElectionDTO, Election> {
    private final Mapper<ValidationSettingDTO, ValidationSetting> mapper;
    private final ModuleRepository moduleRepository;
    private final StudentRepository studentRepository;
    private final ValidationSettingRepository validationSettingRepository;

    @Override
    public ElectionDTO toDto(Election election) {
        return ElectionDTO
                .builder()
                .id(election.getId())
                .studentEmail(election.getStudent().getEmail())
                .electionValid(election.isElectionValid())
                .electedModules(modulesToModuleNos(election.getElectedModules()))
                .validationSettingDTO(mapper.toDto(election.getValidationSetting()))
                .build();

    }

    @Override
    public Election toInstance(ElectionDTO electionDTO) {
        var election = new Election();
        var modules = moduleRepository.findAllById(electionDTO.getElectedModules());
        var student = fetchStudentById(electionDTO.getStudentEmail());
        var validationSettingDto = electionDTO.getValidationSettingDTO();
        var validationSetting = validationSettingDto == null
                ? new ValidationSetting()
                : fetchValidationSetting(student.getEmail());

        election.setElectedModules(new HashSet<>(modules));
        election.setElectionValid(electionDTO.isElectionValid());
        election.setStudent(student);
        election.setValidationSetting(validationSetting);

        return election;
    }

    private Set<String> modulesToModuleNos(Set<Module> modules) {
        return modules
                .stream()
                .map(Module::getModuleNo)
                .collect(Collectors.toSet());
    }

    private ValidationSetting fetchValidationSetting(String id) {
        return validationSettingRepository.findValidationSettingByStudentMail(id).orElseThrow(() -> {
            var formatString =
                    ResourceBundleMessageLoader.getMessage(ResourceMessageConstants.ERROR_VALIDATION_SETTING_NOT_FOUND);
            return new ValidationSettingNotFoundException(String.format(formatString, id));
        });
    }

    private Student fetchStudentById(String id) {
        return studentRepository.findById(id).orElseThrow(() -> {
            var formatString =
                    ResourceBundleMessageLoader.getMessage(ResourceMessageConstants.ERROR_STUDENT_NOT_FOUND);
            return new StudentNotFoundException(String.format(formatString, id));
        });
    }


}
