package ch.zhaw.vorwahlen.exception;


/**
 * This record is used by the {@link ApiExceptionHandler} to create custom error responses.
 */
public record ErrorResponse(String message) {}
