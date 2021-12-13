package ch.zhaw.vorwahlen.exception;

import ch.zhaw.vorwahlen.config.ResourceBundleMessageLoader;
import ch.zhaw.vorwahlen.constants.ResourceMessageConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.context.support.DefaultMessageSourceResolvable;
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
     * @param ex the caught exception
     * @param request {@link WebRequest}
     * @return ResponseEntity<Object>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        log.severe(ex.getLocalizedMessage());
        var error = new ErrorResponse(ResourceBundleMessageLoader.getMessage(ResourceMessageConstants.ERROR_INTERNAL_SERVER_ERROR));
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Catch session exceptions and returns it with the information what went wrong.
     * @param ex the caught exception
     * @param request {@link WebRequest}
     * @return ResponseEntity<Object>
     */
    @ExceptionHandler(SessionNotFoundException.class)
    public ResponseEntity<Object> handleSessionAuthenticationException(Exception ex, WebRequest request) {
        log.fine(ex.getLocalizedMessage());
        var error = new ErrorResponse(ex.getLocalizedMessage());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Catch conflict exceptions and returns it with the information what went wrong.
     * @param ex the caught exception
     * @param request {@link WebRequest}
     * @return ResponseEntity<Object>
     */
    @ExceptionHandler({ MailTemplateConflictException.class, ModuleConflictException.class,
                        ModuleElectionConflictException.class, PageTextConflictException.class,
                        StudentConflictException.class })
    public ResponseEntity<Object> handleConflictException(Exception ex, WebRequest request) {
        log.fine(ex.getLocalizedMessage());
        var error = new ErrorResponse(ex.getLocalizedMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    /**
     * Catch not found exceptions and returns it with the information what went wrong.
     * @param ex the caught exception
     * @param request {@link WebRequest}
     * @return ResponseEntity<Object>
     */
    @ExceptionHandler({ UserNotFoundException.class, StudentNotFoundException.class,
                        MailTemplateNotFoundException.class, PageTextNotFoundException.class,
                        ModuleNotFoundException.class, ModuleElectionNotFoundException.class,
                        EventoDataNotFoundException.class })
    public ResponseEntity<Object> handleNotFoundExceptions(Exception ex, WebRequest request) {
        log.fine(ex.getLocalizedMessage());
        var error = new ErrorResponse(ex.getLocalizedMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Catch import export exceptions and returns it with the information what went wrong.
     * @param ex the caught exception
     * @param request {@link WebRequest}
     * @return ResponseEntity<Object>
     */
    @ExceptionHandler({ ExportException.class, ImportException.class })
    public ResponseEntity<Object> handleImportExportException(Exception ex, WebRequest request) {
        log.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        var error = new ErrorResponse(ex.getLocalizedMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Catch bad requests exception and returns it with the information what went wrong.
     * @param ex the caught exception
     * @param request {@link WebRequest}
     * @return ResponseEntity<Object>
     */
    @ExceptionHandler({ UserTypeInvalidException.class, ElectionBadRequestException.class })
    public ResponseEntity<Object> handleBadRequestException(Exception ex, WebRequest request) {
        log.log(Level.FINE, ex.getLocalizedMessage(), ex);
        var error = new ErrorResponse(ex.getLocalizedMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
                                                                  HttpStatus status, WebRequest request) {
        var message = ResourceBundleMessageLoader.getMessage(ResourceMessageConstants.ERROR_METHOD_ARGUMENT_NOT_VALID);
        var causes = ex
                .getBindingResult()
                .getFieldErrors()
                .stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList();
        log.log(Level.FINE, message, ex);
        var error = new ErrorResponse(message, causes);
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
