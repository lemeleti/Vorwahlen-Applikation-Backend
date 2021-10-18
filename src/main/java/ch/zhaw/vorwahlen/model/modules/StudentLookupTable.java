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
public enum StudentLookupTable implements LookupTable<StudentLookupTable> {
    EMAIL("E-Mail", -1),
    NAME("Name", -1),
    CLAZZ("Klasse", -1);

    private final String cellHeaderName;
    @Setter
    private int cellNumber;
}
