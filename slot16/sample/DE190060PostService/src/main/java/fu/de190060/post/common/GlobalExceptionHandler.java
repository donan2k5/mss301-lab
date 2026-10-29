package fu.de190060.post.common;

import fu.de190060.post.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Bean validation failure on the request body -> status 2 / HTTP 406. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(ApiResponse.error(2, message));
    }

    /** Malformed JSON, unparsable date or wrong value type -> status 2 / HTTP 406. */
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResponse> handleUnreadable(Exception ex) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                .body(ApiResponse.error(2, "Invalid request data (dates must use dd/MM/yyyy)"));
    }

    /** Anything unexpected -> status 0 / HTTP 500 in the ApiResponse format. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleAll(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(0, "Internal server error: " + ex.getMessage()));
    }
}
