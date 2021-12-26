package ch.zhaw.vorwahlen.model.core.module;

import ch.zhaw.vorwahlen.model.ExecutionSemester;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

/**
 * Model / Entity class for a module.
 */
@Entity
@Table(name = "modules")
@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class Module {
    @Id
    private String moduleNo;
    private String shortModuleNo;
    private String moduleTitle;
    private int moduleId;
    private String moduleGroup;
    private String institute;
    private byte credits;
    private String language;
    private ExecutionSemester semester;
    private String consecutiveModuleNo;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Module that)) return false;
        return getModuleId() == that.getModuleId()
                && getCredits() == that.getCredits()
                && Objects.equals(getModuleNo(), that.getModuleNo())
                && Objects.equals(getShortModuleNo(), that.getShortModuleNo())
                && Objects.equals(getModuleTitle(), that.getModuleTitle())
                && Objects.equals(getModuleGroup(), that.getModuleGroup())
                && Objects.equals(getInstitute(), that.getInstitute())
                && Objects.equals(getLanguage(), that.getLanguage())
                && getSemester() == that.getSemester()
                && Objects.equals(getConsecutiveModuleNo(), that.getConsecutiveModuleNo());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getModuleNo(), getShortModuleNo(), getModuleTitle(), getModuleId(), getModuleGroup(),
                            getInstitute(), getCredits(), getLanguage(), getSemester(), getConsecutiveModuleNo());
    }

}
