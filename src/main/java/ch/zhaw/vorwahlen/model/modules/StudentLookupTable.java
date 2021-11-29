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
public enum StudentLookupTable implements LookupTable<StudentLookupTable> {
    EMAIL("E-Mail"),
    NAME("Name"),
    CLAZZ("Klasse");

    private final String cellHeaderName;
    @Setter
    private int cellNumber = -1;
}
