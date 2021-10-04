package ch.zhaw.vorwahlen.model.modules;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@ToString
// Todo search a better name
public enum ModuleStringTable {
    NO("Modulkürzel", -1),
    SHORT_NO("Stammkürzel(Farbcode nach Modultafel)", -1),
    ID("Modul-ID", -1),
    GROUP("Modulgruppe", -1),
    IP("IP-Modul", -1),
    INSTITUTE("Institut/Zentrum", -1),
    CREDITS("Credits/SWL", -1),
    LANGUAGE("Unterrichtssprache", -1);

    private static final Map<String, ModuleStringTable> cellNameMap = new HashMap<>();

    private final String cellHeaderName;
    @Setter @Getter
    private int cellNumber;

    static {
        for (ModuleStringTable moduleTranslator : ModuleStringTable.values()) {
            cellNameMap.put(moduleTranslator.cellHeaderName, moduleTranslator);
        }
    }

    public static ModuleStringTable getConstantByValue(String cellHeaderName) {
        return cellNameMap.get(cellHeaderName);
    }
}
