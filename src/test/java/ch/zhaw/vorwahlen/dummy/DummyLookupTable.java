package ch.zhaw.vorwahlen.dummy;

import ch.zhaw.vorwahlen.model.modules.parser.LookupTable;

public enum DummyLookupTable implements LookupTable<DummyLookupTable> {
    NO("Modulkürzel"),
    SHORT_NO("Stammkürzel(Farbcode nach Modultafel)"),
    TITLE("Modulbezeichnung Deutsch(Farbcode nach Curriculum)"),
    NO_COLUMN("Non existing column");

    private final String cellHeaderName;
    private int cellNumber;

    DummyLookupTable(String cellHeaderName) {
        this.cellHeaderName = cellHeaderName;
        this.cellNumber = -1;
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
