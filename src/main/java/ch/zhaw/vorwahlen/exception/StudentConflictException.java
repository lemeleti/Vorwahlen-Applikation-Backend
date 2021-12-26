package ch.zhaw.vorwahlen.exception;

public class StudentConflictException extends RuntimeException {
    public StudentConflictException(String message) {
        super(message);
    }
}
