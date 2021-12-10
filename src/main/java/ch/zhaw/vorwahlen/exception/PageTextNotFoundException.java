package ch.zhaw.vorwahlen.exception;

public class PageTextNotFoundException extends RuntimeException {
    public PageTextNotFoundException(String message) {
        super(message);
    }
}
