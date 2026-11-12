package fr.esgi.user_api.exceptions;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import fr.esgi.user_api.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice 
@Slf4j 
public class GlobalExceptionHandler {
    
    @ExceptionHandler (EmailAlreadyUsedException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsException(EmailAlreadyUsedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            ErrorResponse.builder()
            .status(409)
            .error("Conflict")
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build()
        );
    }

    @ExceptionHandler (Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ErrorResponse.builder()
            .status(500)
            .error("Internal Server Error")
            .message("An unexpected error occurred. Please try again later.")
            .timestamp(LocalDateTime.now())
            .build()
        );
    }
    
}
