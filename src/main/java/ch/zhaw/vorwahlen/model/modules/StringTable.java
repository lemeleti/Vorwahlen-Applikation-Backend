package ch.zhaw.vorwahlen.model.modules;

public interface StringTable<S extends Enum<S>> {
    void setCellNumber(int index);
    String getCellHeaderName();
    int getCellNumber();

    static <S extends StringTable<?>> S getConstantByValue(String data, Class<? extends StringTable<?>> type) {
        for (var en : type.getEnumConstants()) {
            if (en.getCellHeaderName().equals(data))
                return (S) en;
        }
        return null;
    }
}
