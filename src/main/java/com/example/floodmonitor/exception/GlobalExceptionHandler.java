package com.example.floodmonitor.exception;

import com.example.floodmonitor.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.lang.NonNull;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private final Clock clock;
    public GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler({StationNotFoundException.class, NoMeasurementForStationException.class})
    public ResponseEntity<ApiError> handleNotFound(RuntimeException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).body(createApiError(status, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiError> handleInvalidRequest(InvalidRequestException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(createApiError(status, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleParameterTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = "Parameter '" + ex.getName() + "' has an invalid value: '" + ex.getValue() + "'";
        return ResponseEntity.status(status).body(createApiError(status, message, request.getRequestURI()));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex, @NonNull HttpHeaders headers, @NonNull HttpStatusCode statusCode, @NonNull WebRequest request) {
        HttpStatus status = HttpStatus.NOT_ACCEPTABLE;
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        ApiError error = createApiError(status, ex.getMessage(), path);
        return ResponseEntity.status(status).headers(headers).contentType(MediaType.APPLICATION_JSON).body(error);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, @NonNull HttpHeaders headers, HttpStatusCode statusCode, @NonNull WebRequest request) {
        HttpStatus status = HttpStatus.valueOf(statusCode.value());
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        ApiError error = createApiError(status, ex.getMessage(), path);
        return ResponseEntity.status(status).headers(headers).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        logger.error("Unhandled exception", ex);
        return ResponseEntity.status(status)
                .body(createApiError(status, "An unexpected error occurred", request.getRequestURI()));
    }

    private ApiError createApiError(HttpStatus status, String message, String requestURI) {
        return new ApiError(Instant.now(clock).truncatedTo(ChronoUnit.SECONDS), status.value(), status.getReasonPhrase(), message, requestURI);
    }
}
