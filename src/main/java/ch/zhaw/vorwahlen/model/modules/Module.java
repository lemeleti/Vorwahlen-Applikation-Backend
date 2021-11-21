package ch.zhaw.vorwahlen.model.modules;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

/**
 * Model / Entity class for a module
 */
@Entity
@Table(name = "modules")
@Getter @Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class Module {
    @Id
    private String moduleNo;
    private String shortModuleNo;
    private String moduleTitle;
    private int moduleId;
    private String moduleGroup;
    private boolean isIPModule;
    private String institute;
    private byte credits;
    private String language;
    private String fullTimeSemester;
    private String partTimeSemester;
    private String consecutiveModuleNo;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Module module = (Module) o;
        return moduleNo != null && Objects.equals(moduleNo, module.moduleNo);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
