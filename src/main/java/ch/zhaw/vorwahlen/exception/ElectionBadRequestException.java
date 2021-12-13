package ch.zhaw.vorwahlen.exception;

public class ElectionBadRequestException extends RuntimeException {
    public ElectionBadRequestException(String message) {
        super(message);
    }
}
