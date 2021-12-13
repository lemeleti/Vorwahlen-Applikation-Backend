package ch.zhaw.vorwahlen.exception;

public class MailTemplateConflictException extends RuntimeException {
    public MailTemplateConflictException(String message) {
        super(message);
    }
}
