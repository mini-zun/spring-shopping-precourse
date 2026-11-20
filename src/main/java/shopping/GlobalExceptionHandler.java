package shopping;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("입력값이 올바르지 않습니다.");
        ErrorResponse response = new ErrorResponse("INVALID_REQUEST", message);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFound(
            ProductNotFoundException exception) {
        ErrorResponse response = new ErrorResponse(
                "PRODUCT_NOT_FOUND", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ProfanityDetectedException.class)
    public ResponseEntity<ErrorResponse> handleProfanityDetected(
            ProfanityDetectedException exception) {
        ErrorResponse response = new ErrorResponse(
                "PROFANITY_DETECTED", exception.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ProfanityServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleProfanityServiceUnavailable(
            ProfanityServiceUnavailableException exception) {
        ErrorResponse response = new ErrorResponse(
                "PROFANITY_SERVICE_UNAVAILABLE", exception.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableMessage(
            HttpMessageNotReadableException exception) {
        ErrorResponse response = new ErrorResponse(
                "INVALID_REQUEST", "요청 JSON 형식이나 값의 타입이 올바르지 않습니다.");
        return ResponseEntity.badRequest().body(response);
    }
}
