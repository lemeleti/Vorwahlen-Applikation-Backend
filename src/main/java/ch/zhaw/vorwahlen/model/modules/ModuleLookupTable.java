package ch.zhaw.vorwahlen.model.modules;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Lookup table to parse the module list Excel.
 */
@RequiredArgsConstructor
@Getter
@ToString
public enum ModuleLookupTable implements LookupTable<ModuleLookupTable> {
    NO("Modulkürzel"),
    SHORT_NO("Stammkürzel(Farbcode nach Modultafel)"),
    TITLE("Modulbezeichnung Deutsch(Farbcode nach Curriculum)"),
    ID("Modul-ID"),
    GROUP("Modulgruppe"),
    IP("IP-Modul"),
    INSTITUTE("Institut/Zentrum"),
    CREDITS("Credits/SWL"),
    LANGUAGE("Unterrichtssprache"),
    SEMESTER_FULL_TIME("IT-VZ"),
    SEMESTER_PART_TIME("IT-TZ");

    private final String cellHeaderName;
    @Setter
    private int cellNumber = -1;
}
