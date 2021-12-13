package ch.zhaw.vorwahlen.exception;

public class ModuleConflictException extends RuntimeException {
    public ModuleConflictException(String message) {
        super(message);
    }
}
