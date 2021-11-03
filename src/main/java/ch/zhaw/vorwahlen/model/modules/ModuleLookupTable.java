package ch.zhaw.vorwahlen.model.modules;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Lookup table to parse the module list Excel.
 */
@AllArgsConstructor
@Getter
@ToString
public enum ModuleLookupTable implements LookupTable<ModuleLookupTable> {
    NO("Modulkürzel", -1),
    SHORT_NO("Stammkürzel(Farbcode nach Modultafel)", -1),
    TITLE("Modulbezeichnung Deutsch(Farbcode nach Curriculum)", -1),
    ID("Modul-ID", -1),
    GROUP("Modulgruppe", -1),
    IP("IP-Modul", -1),
    INSTITUTE("Institut/Zentrum", -1),
    CREDITS("Credits/SWL", -1),
    LANGUAGE("Unterrichtssprache", -1),
    SEMESTER_FULL_TIME("IT-VZ", -1),
    SEMESTER_PART_TIME("IT-TZ", -1);

    private final String cellHeaderName;
    @Setter
    private int cellNumber;
}
