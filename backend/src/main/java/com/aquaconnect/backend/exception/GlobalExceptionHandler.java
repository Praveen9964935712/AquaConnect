package com.aquaconnect.backend.exception;

import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleConflict(DataIntegrityViolationException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Email is already registered"));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid email or password"));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ResourceNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Resource not found"));
    }

    @ExceptionHandler(InvalidIncidentTransitionException.class)
    public ResponseEntity<Map<String, String>> handleInvalidTransition() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Invalid incident status transition"));
    }

    @ExceptionHandler(InvalidSupportingReportException.class)
    public ResponseEntity<Map<String, String>> handleInvalidSupportingReport() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Invalid supporting relationship"));
    }

    @ExceptionHandler(InvalidWorkOrderTransitionException.class)
    public ResponseEntity<Map<String, String>> handleInvalidWorkOrderTransition() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Invalid work-order status transition"));
    }

    @ExceptionHandler(InvalidWorkOrderException.class)
    public ResponseEntity<Map<String, String>> handleInvalidWorkOrder() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Invalid work-order operation"));
    }

    @ExceptionHandler(InvalidEvidenceException.class)
    public ResponseEntity<Map<String, String>> handleInvalidEvidence() {
        return ResponseEntity.badRequest().body(Map.of("error", "Invalid evidence"));
    }

    @ExceptionHandler(InvalidAuthorityVerificationException.class)
    public ResponseEntity<Map<String, String>> handleInvalidAuthorityVerification() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Invalid authority verification"));
    }

    @ExceptionHandler(InvalidResolutionConfirmationException.class)
    public ResponseEntity<Map<String, String>> handleInvalidResolutionConfirmation() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Invalid resolution confirmation"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument() {
        return ResponseEntity.badRequest().body(Map.of("error", "Invalid request data"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation() {
        return ResponseEntity.badRequest().body(Map.of("error", "Invalid request data"));
    }
}
