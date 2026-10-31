package com.example.floodmonitor.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ErrorResponse> handleNotAcceptable(HttpMediaTypeNotAcceptableException ex, HttpServletRequest request) {
        return errorResponse(HttpStatus.NOT_ACCEPTABLE, "Das angeforderte Antwortformat wird nicht unterstützt.", request);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        return errorResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Der Request-Medientyp wird nicht unterstützt.", request);
    }

    // 404 - Station nicht gefunden
    @ExceptionHandler(StationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleStationNotFound(StationNotFoundException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // 400 - Ungültige Eingaben, falsche Parameter, Zeiträume oder Limit-Werte
    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Ungültige Eingabe oder falsches Format: " + ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // 500 - Interner Serverfehler (garantiert OHNE Stacktrace für den Client)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Ein unerwarteter interner Fehler ist aufgetreten.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    private ResponseEntity<ErrorResponse> errorResponse(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(),
                message, request.getRequestURI());
        return ResponseEntity.status(status)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(error);
    }
}
