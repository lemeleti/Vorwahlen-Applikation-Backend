package ch.zhaw.vorwahlen.exception;

public class ElectionConflictException extends RuntimeException {
    public ElectionConflictException(String message) {
        super(message);
    }
}
