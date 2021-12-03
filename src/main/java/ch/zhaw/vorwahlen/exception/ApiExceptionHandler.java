package ch.zhaw.vorwahlen.exception;

import ch.zhaw.vorwahlen.config.ResourceBundleMessageLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.logging.Level;

/**
 * This class catches and handles all exceptions that occur inside the controllers.
 */
@ControllerAdvice
@RequiredArgsConstructor
@Log
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {


    /**
     * Catches all {@link Exception} and returns it with the information what went wrong.
     * @param ex {@link Exception}
     * @param request {@link WebRequest}
     * @return ResponseEntity<Object>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        var error = new ErrorResponse(ResourceBundleMessageLoader.getMessage("error.interner_server_error"));
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(SessionNotFoundException.class)
    public ResponseEntity<Object> handleSessionAuthenticationException(Exception ex, WebRequest request) {
        log.severe(ex.getLocalizedMessage());
        var error = new ErrorResponse(ex.getLocalizedMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(Exception ex, WebRequest request) {
        log.severe(ex.getLocalizedMessage());
        var error = new ErrorResponse(ex.getLocalizedMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({ ExportException.class, ImportException.class })
    public ResponseEntity<Object> handleImportExportException(Exception ex, WebRequest request) {
        log.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        var error = new ErrorResponse(ex.getLocalizedMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
                                                                  HttpStatus status, WebRequest request) {
        var message = ResourceBundleMessageLoader.getMessage("error.method_argument_not_valid");
        var messageBuilder = new StringBuilder();
        messageBuilder.append(message);
        messageBuilder.append(System.lineSeparator());
        ex
                .getBindingResult()
                .getFieldErrors()
                .forEach(fieldError ->  {
                    messageBuilder.append(fieldError.getDefaultMessage());
                    messageBuilder.append(System.lineSeparator());
                });
        log.log(Level.SEVERE, messageBuilder.toString(), ex);
        var error = new ErrorResponse(messageBuilder.toString());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers,
                                                                  HttpStatus status, WebRequest request) {
        return handleAllExceptions(ex, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(HttpMessageNotWritableException ex, HttpHeaders headers,
                                                                  HttpStatus status, WebRequest request) {
        return handleAllExceptions(ex, request);
    }

}
