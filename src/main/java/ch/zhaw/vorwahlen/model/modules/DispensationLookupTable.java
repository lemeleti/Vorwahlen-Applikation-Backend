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
public enum DispensationLookupTable implements LookupTable<DispensationLookupTable> {
    EMAIL("E-Mail", -1),
    PA("Dispensation PA [Credits]", -1),
    WPM("Dispensation WPM [Credits]", -1);

    private final String cellHeaderName;
    @Setter
    private int cellNumber;
}
