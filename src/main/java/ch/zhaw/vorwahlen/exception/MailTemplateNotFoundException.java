package ch.zhaw.vorwahlen.exception;

public class MailTemplateNotFoundException extends RuntimeException {
    public MailTemplateNotFoundException(String message) {
        super(message);
    }
}
