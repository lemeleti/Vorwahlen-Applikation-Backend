package ch.zhaw.vorwahlen.model.modules;

import lombok.*;

import javax.persistence.*;
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
    //Todo remove either from EventoData Class or here.
    private String language;
    // Todo add Durchführungssemester für VZ + TZ
}
