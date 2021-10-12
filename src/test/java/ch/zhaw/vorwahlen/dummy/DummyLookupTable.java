package ch.zhaw.vorwahlen.dummy;

import ch.zhaw.vorwahlen.model.modules.LookupTable;

public enum DummyLookupTable implements LookupTable<DummyLookupTable> {
    NO("Modulkürzel", -1),
    SHORT_NO("Stammkürzel(Farbcode nach Modultafel)", -1),
    TITLE("Modulbezeichnung Deutsch(Farbcode nach Curriculum)", -1),
    NO_COLUMN("Non existing column", -1);

    private final String cellHeaderName;
    private int cellNumber;

    DummyLookupTable(String cellHeaderName, int cellNumber) {
        this.cellHeaderName = cellHeaderName;
        this.cellNumber = cellNumber;
    }

    @Override
    public String getCellHeaderName() {
        return cellHeaderName;
    }

    @Override
    public int getCellNumber() {
        return cellNumber;
    }

    @Override
    public void setCellNumber(int cellNumber) {
        this.cellNumber = cellNumber;
    }
}
