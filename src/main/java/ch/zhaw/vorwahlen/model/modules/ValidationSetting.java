package ch.zhaw.vorwahlen.model.modules;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Objects;

/**
 * Model/Entity class for validation settings.
 */
@Entity
@Getter @Setter
@NoArgsConstructor
public class ValidationSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "tinyint(1) default 0")
    private boolean isRepetent;

    @Getter(AccessLevel.NONE)
    @Column(columnDefinition = "tinyint(1) default 0")
    private boolean alreadyElectedTwoConsecutiveModules;

    @Column(columnDefinition = "tinyint(1) default 0")
    private boolean isSkipConsecutiveModuleCheck;

    @Column(columnDefinition = "tinyint default 0")
    private int electedContextModulesInFirstElection;

    public boolean hadAlreadyElectedTwoConsecutiveModules() {
        return alreadyElectedTwoConsecutiveModules;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ValidationSetting that)) return false;
        return isRepetent() == that.isRepetent()
                && alreadyElectedTwoConsecutiveModules == that.alreadyElectedTwoConsecutiveModules
                && isSkipConsecutiveModuleCheck() == that.isSkipConsecutiveModuleCheck()
                && getElectedContextModulesInFirstElection() == that.getElectedContextModulesInFirstElection()
                && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), isRepetent(), alreadyElectedTwoConsecutiveModules, isSkipConsecutiveModuleCheck(),
                            getElectedContextModulesInFirstElection());
    }

}
