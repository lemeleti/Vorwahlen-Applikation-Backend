package ch.zhaw.vorwahlen.config;

import ch.zhaw.vorwahlen.constants.ResourceMessageConstants;
import ch.zhaw.vorwahlen.exception.StudentNotFoundException;
import ch.zhaw.vorwahlen.exporter.ExcelElectionExporter;
import ch.zhaw.vorwahlen.exporter.ElectionExporter;
import ch.zhaw.vorwahlen.model.modulestructure.ElectionSemesters;
import ch.zhaw.vorwahlen.model.core.student.Student;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleDefinition;
import ch.zhaw.vorwahlen.repository.StudentRepository;
import ch.zhaw.vorwahlen.security.model.User;
import ch.zhaw.vorwahlen.validation.ElectionValidator;
import ch.zhaw.vorwahlen.validation.FullTimeElectionValidator;
import ch.zhaw.vorwahlen.validation.PartTimeElectionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Configuration for the election.
 */
@Configuration
@RequiredArgsConstructor
public class ElectionConfig {

    private final StudentRepository studentRepository;

    /**
     * Returns the validator based on the current student.
     * @return ElectionValidator
     */
    @Bean
    @Scope(value = BeanDefinition.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public ElectionValidator electionValidator() {
        var student = getStudentFromSecurityContext();
        ElectionValidator electionValidator = new FullTimeElectionValidator(student);
        if (student.isTZ()) {
            electionValidator = new PartTimeElectionValidator(student);
        }
        return electionValidator;
    }

    /**
     * Returns the module definition. Values loaded based on the current student.
     * @return ModuleDefinition
     */
    @Bean
    @Primary
    @Scope(value = BeanDefinition.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public ModuleDefinition moduleDefinition() {
        var student = getStudentFromSecurityContext();
        return student.isTZ() ? moduleDefinitionPartTime() : moduleDefinitionFullTime();
    }

    /**
     * Returns a new module definition values loaded by prefix tz.
     * @return ModuleDefinition
     */
    @Bean
    @ConfigurationProperties(prefix = "tz")
    public ModuleDefinition moduleDefinitionPartTime() {
        return new ModuleDefinition();
    }

    /**
     * Returns a new module definition values loaded by prefix vz.
     * @return ModuleDefinition
     */
    @Bean
    @ConfigurationProperties(prefix = "vz")
    public ModuleDefinition moduleDefinitionFullTime() {
        return new ModuleDefinition();
    }

    /**
     * Returns a new election semesters instance. Values loaded by student prefix.
     * @return ElectionSemesters
     */
    @Bean
    @ConfigurationProperties(prefix = "student")
    public ElectionSemesters electionSemesters() {
        return new ElectionSemesters();
    }

    /**
     * Returns a new election exporter instance.
     * @return ElectionExporter
     */
    @Bean
    public ElectionExporter electionExporter() {
        return new ExcelElectionExporter();
    }

    private Student getStudentFromSecurityContext() {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var id = user.getMail();
        return studentRepository.findById(id)
                .orElseThrow(() -> {
                    var formatString =
                            ResourceBundleMessageLoader.getMessage(ResourceMessageConstants.ERROR_STUDENT_NOT_FOUND);
                    return new StudentNotFoundException(String.format(formatString, id));
                });
    }
}
