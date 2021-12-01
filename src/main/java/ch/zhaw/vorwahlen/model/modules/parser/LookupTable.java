package ch.zhaw.vorwahlen.model.modules.parser;

/**
 * Contract for lookup table.<br>
 * Usage: {@code enum ModuleLookupTable implements LookupTable<ModuleLookupTable>}
 *
 * @param <S> the to be implemented enum.
 */
public interface LookupTable<S extends Enum<S>> {
    void setCellNumber(int index);
    String getCellHeaderName();
    int getCellNumber();

    /**
     * Returns lookup table constant by cell header name.
     * @param data cell header name.
     * @param type enum which extends this interface.
     * @param <S> the to be implemented enum.
     * @return instance of provided enum or null.
     */
    static <S extends LookupTable<?>> S getConstantByValue(String data, Class<? extends LookupTable<?>> type) {
        for (var en : type.getEnumConstants()) {
            if (en.getCellHeaderName().equals(data)) {
                return (S) en;
            }
        }
        return null;
    }
}
