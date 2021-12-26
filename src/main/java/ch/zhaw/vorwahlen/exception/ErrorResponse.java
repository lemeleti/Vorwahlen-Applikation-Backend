package ch.zhaw.vorwahlen.exception;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used by the {@link ApiExceptionHandler} to create custom error responses.
 */
@RequiredArgsConstructor
@AllArgsConstructor
@Data
public class ErrorResponse {
    private final String message;
    List<String> causes = new ArrayList<>();
}
