package ch.zhaw.vorwahlen.exception;

public class ValidationSettingNotFoundException extends RuntimeException {
    public ValidationSettingNotFoundException(String message) {
        super(message);
    }
}
