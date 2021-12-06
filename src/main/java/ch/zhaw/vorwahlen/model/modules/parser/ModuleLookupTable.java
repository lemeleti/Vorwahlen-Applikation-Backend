package ch.zhaw.vorwahlen.model.modules.parser;

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
    INSTITUTE("Institut/Zentrum"),
    CREDITS("Credits/SWL"),
    LANGUAGE("Unterrichtssprache"),
    SEMESTER_FULL_TIME("IT-VZ");

    private final String cellHeaderName;
    @Setter
    private int cellNumber = -1;
}
