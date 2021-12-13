package ch.zhaw.vorwahlen.exception;

public class ModuleElectionConflictException extends RuntimeException {
    public ModuleElectionConflictException(String message) {
        super(message);
    }
}
