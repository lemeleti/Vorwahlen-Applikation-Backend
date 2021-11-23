package ch.zhaw.vorwahlen.config;

import ch.zhaw.vorwahlen.exporter.ExcelModuleElectionExporter;
import ch.zhaw.vorwahlen.exporter.ModuleElectionExporter;
import ch.zhaw.vorwahlen.model.modules.Student;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleDefinition;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleDefinitionFullTime;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleDefinitionPartTime;
import ch.zhaw.vorwahlen.modulevalidation.ElectionValidator;
import ch.zhaw.vorwahlen.modulevalidation.FullTimeElectionValidator;
import ch.zhaw.vorwahlen.modulevalidation.PartTimeElectionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.function.Function;

@Configuration
@RequiredArgsConstructor
public class ElectionConfig {
    public static final String ELECTION_VALIDATOR="electionValidator";
    public static final String MODULE_STRUCTURE="moduleStructure";
    private final ModuleDefinitionFullTime structureFullTime;
    private final ModuleDefinitionPartTime structurePartTime;

    @Bean(name = ELECTION_VALIDATOR)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ElectionValidator electionValidator(Student student) {
        ElectionValidator electionValidator = new FullTimeElectionValidator(student);
        if (student.isTZ()) {
            electionValidator = new PartTimeElectionValidator(student);
        }
        return electionValidator;
    }

    @Bean
    public Function<Student, ElectionValidator> electionValidatorFunction() {
        return this::electionValidator;
    }

    @Bean(name = MODULE_STRUCTURE)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ModuleDefinition moduleStructure(Student student) {
        if (student.isTZ()) {
            return structurePartTime;
        }
        return structureFullTime;
    }

    @Bean
    public Function<Student, ModuleDefinition> moduleStructureFunction() {
        return this::moduleStructure;
    }

    @Bean
    public ModuleElectionExporter moduleElectionExporter() {
        return new ExcelModuleElectionExporter();
    }

}
