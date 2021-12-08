package ch.zhaw.vorwahlen.mapper;

/**
 * Contract for a mapper where T is the dto class and S is the concrete class.
 */
public interface Mapper<T, S> {

    /**
     * Map concrete instance to it's dto instance.
     * @param param the concrete instance.
     * @return the new dto instance.
     */
    T toDto(S param);

    /**
     * Map the dto instance to it's concrete instance.
     * @param param the dto instance.
     * @return the new concrete instance or throws {@link UnsupportedOperationException} when not implemented in the concrete mappers.
     */
    default S toInstance(T param) {
        throw new UnsupportedOperationException();
    }
}
