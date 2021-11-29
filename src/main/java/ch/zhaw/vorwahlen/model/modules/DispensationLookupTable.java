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
public enum DispensationLookupTable implements LookupTable<DispensationLookupTable> {
    EMAIL("E-Mail"),
    PA("Dispensation PA [Credits]"),
    WPM("Dispensation WPM [Credits]");

    private final String cellHeaderName;
    @Setter
    private int cellNumber = -1;
}
