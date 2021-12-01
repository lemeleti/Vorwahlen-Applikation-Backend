package ch.zhaw.vorwahlen.model.modules;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ElectionStatus {

    private final ElectionStatusElement subjectValidation;
    private final ElectionStatusElement contextValidation;
    private final ElectionStatusElement interdisciplinaryValidation;
    private final ElectionStatusElement additionalValidation;

    private boolean isValid;

    public ElectionStatus() {
        subjectValidation = new ElectionStatusElement(this);
        contextValidation = new ElectionStatusElement(this);
        interdisciplinaryValidation = new ElectionStatusElement(this);
        additionalValidation = new ElectionStatusElement(this);
    }

    protected void updateIsValid() {
        isValid = subjectValidation.isValid
                && contextValidation.isValid
                && interdisciplinaryValidation.isValid
                && additionalValidation.isValid;
    }

    @Getter @Setter
    public static class ElectionStatusElement {

        private ElectionStatus electionStatus;
        private ModuleCategory moduleCategory;
        private boolean isValid;

        private ElectionStatusElement(ElectionStatus electionStatus) {
            this.electionStatus = electionStatus;
        }

        @Setter(AccessLevel.NONE)
        private List<String> reasons = new ArrayList<>();

        public void addReason(String reason) {
            reasons.add(reason);
        }

        public void setValid(boolean valid) {
            isValid = valid;
            electionStatus.updateIsValid();
        }

        public void andValid(boolean valid) {
            isValid = isValid && valid;
            electionStatus.updateIsValid();
        }

    }

}
