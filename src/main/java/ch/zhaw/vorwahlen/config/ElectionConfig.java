package ch.zhaw.vorwahlen.config;

import ch.zhaw.vorwahlen.model.modules.Student;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleStructure;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleStructureFullTime;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleStructurePartTime;
import ch.zhaw.vorwahlen.modulevalidation.ElectionValidator;
import ch.zhaw.vorwahlen.modulevalidation.FullTimeElectionValidator;
import ch.zhaw.vorwahlen.modulevalidation.PartTimeElectionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@RequiredArgsConstructor
public class ElectionConfig {
    public static final String ELECTION_VALIDATOR="electionValidator";
    public static final String MODULE_STRUCTURE="moduleStructure";
    private final ModuleStructureFullTime structureFullTime;
    private final ModuleStructurePartTime structurePartTime;

    @Bean(name = ELECTION_VALIDATOR)
    @Scope(value = "prototype")
    public ElectionValidator electionValidator(Student student) {
        ElectionValidator electionValidator = new FullTimeElectionValidator(student);
        if (student.isTZ()) {
            electionValidator = new PartTimeElectionValidator(student);
        }
        return electionValidator;
    }

    @Bean(name = MODULE_STRUCTURE)
    @Scope(value = "prototype")
    public ModuleStructure moduleStructure(Student student) {
        if (student.isTZ()) {
            return structurePartTime;
        }
        return structureFullTime;
    }

}
