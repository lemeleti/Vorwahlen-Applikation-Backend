package ch.zhaw.vorwahlen.model.modules;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Model / Entity class for a module
 */
@Entity
@Table(name = "modules")
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

    @OneToOne
    private Module consecutiveModule;
}
