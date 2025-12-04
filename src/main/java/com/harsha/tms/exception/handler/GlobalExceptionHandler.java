package com.harsha.tms.exception.handler;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.harsha.tms.exception.InsufficientCapacityException;
import com.harsha.tms.exception.InvalidStatusTransitionException;
import com.harsha.tms.exception.LoadAlreadyBookedException;
import com.harsha.tms.exception.ResourceNotFoundException;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex,
                                                                HttpServletRequest request) {
        return buildResponseEntity(ex, request, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStatusTransition(InvalidStatusTransitionException ex,
                                                                       HttpServletRequest request) {
        return buildResponseEntity(ex, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InsufficientCapacityException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientCapacity(InsufficientCapacityException ex,
                                                                    HttpServletRequest request) {
        return buildResponseEntity(ex, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(LoadAlreadyBookedException.class)
    public ResponseEntity<ErrorResponse> handleLoadAlreadyBooked(LoadAlreadyBookedException ex,
                                                                 HttpServletRequest request) {
        return buildResponseEntity(ex, request, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                      HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        ErrorResponse body = new ErrorResponse(
                Instant.now().toString(),
                "Validation failed: " + message,
                request.getRequestURI()
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex,
                                                           HttpServletRequest request) {
        return buildResponseEntity(ex, request, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<ErrorResponse> buildResponseEntity(Exception ex,
                                                              HttpServletRequest request,
                                                              HttpStatus status) {
        ErrorResponse body = new ErrorResponse(
                Instant.now().toString(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(body, status);
    }

    public static class ErrorResponse {
        private final String timestamp;
        private final String message;
        private final String path;

        public ErrorResponse(String timestamp, String message, String path) {
            this.timestamp = timestamp;
            this.message = message;
            this.path = path;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public String getMessage() {
            return message;
        }

        public String getPath() {
            return path;
        }
    }
}

