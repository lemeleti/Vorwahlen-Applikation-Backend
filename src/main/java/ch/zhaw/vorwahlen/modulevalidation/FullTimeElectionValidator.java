package ch.zhaw.vorwahlen.modulevalidation;

import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import ch.zhaw.vorwahlen.model.modules.Student;

public class FullTimeElectionValidator extends AbstractElectionValidator {
    public FullTimeElectionValidator(Student student) {
        super(student);
    }
    @Override
    public boolean validate(ModuleElection election) {
        return isOverflownEmpty(election)
                && isCreditSumValid(election)
                && validContextModuleElection(election)
                && validSubjectModuleElection(election)
                && validInterdisciplinaryModuleElection(election)
                && validIpModuleElection(election)
                && validConsecutiveModuleElection(election);
    }
}
