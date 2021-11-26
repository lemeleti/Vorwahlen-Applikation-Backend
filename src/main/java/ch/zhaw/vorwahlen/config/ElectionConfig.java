package ch.zhaw.vorwahlen.config;

import ch.zhaw.vorwahlen.exporter.ExcelModuleElectionExporter;
import ch.zhaw.vorwahlen.exporter.ModuleElectionExporter;
import ch.zhaw.vorwahlen.model.modules.Student;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleDefinition;
import ch.zhaw.vorwahlen.model.user.User;
import ch.zhaw.vorwahlen.modulevalidation.ElectionValidator;
import ch.zhaw.vorwahlen.modulevalidation.FullTimeElectionValidator;
import ch.zhaw.vorwahlen.modulevalidation.PartTimeElectionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@RequiredArgsConstructor
public class ElectionConfig {
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

    @Bean
    @Primary
    @Scope(value = BeanDefinition.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public ModuleDefinition moduleStructure() {
        var student = getStudentFromSecurityContext();
        return student.isTZ() ? moduleDefinitionPartTime() : moduleDefinitionFullTime();
    }

    @Bean
    @ConfigurationProperties(prefix = "tz")
    public ModuleDefinition moduleDefinitionPartTime() {
        return new ModuleDefinition();
    }

    @Bean
    @ConfigurationProperties(prefix = "vz")
    public ModuleDefinition moduleDefinitionFullTime() {
        return new ModuleDefinition();
    }

    @Bean
    public ModuleElectionExporter moduleElectionExporter() {
        return new ExcelModuleElectionExporter();
    }

    private Student getStudentFromSecurityContext() {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getStudent();
    }
}
