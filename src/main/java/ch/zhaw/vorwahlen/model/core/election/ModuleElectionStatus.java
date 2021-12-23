package ch.zhaw.vorwahlen.model.core.election;

import ch.zhaw.vorwahlen.model.core.module.ModuleCategory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ModuleElectionStatus {

    private final ModuleElectionStatusElement subjectValidation;
    private final ModuleElectionStatusElement contextValidation;
    private final ModuleElectionStatusElement interdisciplinaryValidation;
    private final ModuleElectionStatusElement additionalValidation;

    private boolean isValid;

    public ModuleElectionStatus() {
        subjectValidation = new ModuleElectionStatusElement(this);
        contextValidation = new ModuleElectionStatusElement(this);
        interdisciplinaryValidation = new ModuleElectionStatusElement(this);
        additionalValidation = new ModuleElectionStatusElement(this);
    }

    protected void updateIsValid() {
        isValid = subjectValidation.isValid
                && contextValidation.isValid
                && interdisciplinaryValidation.isValid
                && additionalValidation.isValid;
    }

    @Getter @Setter
    public static class ModuleElectionStatusElement {

        private ModuleElectionStatus moduleElectionStatus;
        private ModuleCategory moduleCategory;
        private boolean isValid;

        private ModuleElectionStatusElement(ModuleElectionStatus moduleElectionStatus) {
            this.moduleElectionStatus = moduleElectionStatus;
        }

        @Setter(AccessLevel.NONE)
        private List<String> reasons;

        public void addReason(String reason) {
            if(reasons == null) {
                reasons = new ArrayList<>();
            }
            reasons.add(reason);
        }

        public void setValid(boolean valid) {
            isValid = valid;
            moduleElectionStatus.updateIsValid();
        }

        public void andValid(boolean valid) {
            isValid = isValid && valid;
            moduleElectionStatus.updateIsValid();
        }

    }

}
