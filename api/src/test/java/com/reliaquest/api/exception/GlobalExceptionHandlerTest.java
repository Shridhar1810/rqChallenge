package com.reliaquest.api.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the GlobalExceptionHandler class
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleEmployeeNotFoundException should return NOT_FOUND with appropriate error details")
    void handleEmployeeNotFoundExceptionShouldReturnNotFoundWithErrorDetails() {
        String message = "Employee with ID 123 not found";
        EmployeeNotFoundException exception = new EmployeeNotFoundException(message);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = exceptionHandler.handleEmployeeNotFoundException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ApiErrorCode.RESOURCE_NOT_FOUND.getCode(), response.getBody().getErrorCode());
        assertEquals(message, response.getBody().getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatus());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("handleRateLimitedException should return TOO_MANY_REQUESTS with retry header")
    void handleRateLimitedExceptionShouldReturnTooManyRequestsWithRetryHeader() {
        String message = "Rate limit exceeded";
        int retrySeconds = 30;
        RateLimitedException exception = new RateLimitedException(message, null, retrySeconds);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = exceptionHandler.handleRateLimitedException(exception);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ApiErrorCode.EXTERNAL_API_RATE_LIMITED.getCode(), response.getBody().getErrorCode());
        assertTrue(response.getBody().getMessage().contains(String.valueOf(retrySeconds)));
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), response.getBody().getStatus());
        assertNotNull(response.getBody().getTimestamp());
        
        assertTrue(response.getHeaders().containsKey("Retry-After"));
        assertEquals(String.valueOf(retrySeconds), response.getHeaders().getFirst("Retry-After"));
    }

    @Test
    @DisplayName("handleExternalApiException should return appropriate status with error details")
    void handleExternalApiExceptionShouldReturnAppropriateStatusWithErrorDetails() {
        ExternalApiException exception = mock(ExternalApiException.class);
        when(exception.getErrorCode()).thenReturn(ApiErrorCode.EXTERNAL_API_UNAVAILABLE);
        when(exception.getMessage()).thenReturn("External API unavailable");
        when(exception.getHttpStatus()).thenReturn(HttpStatus.SERVICE_UNAVAILABLE);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = exceptionHandler.handleExternalApiException(exception);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ApiErrorCode.EXTERNAL_API_UNAVAILABLE.getCode(), response.getBody().getErrorCode());
        assertEquals("External API unavailable", response.getBody().getMessage());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE.value(), response.getBody().getStatus());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("handleValidationExceptions should return BAD_REQUEST with field errors")
    void handleValidationExceptionsShouldReturnBadRequestWithFieldErrors() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        FieldError nameError = new FieldError("employee", "name", "Name cannot be empty");
        FieldError ageError = new FieldError("employee", "age", "Age must be positive");
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(nameError, ageError));
        
        ResponseEntity<GlobalExceptionHandler.ValidationErrorResponse> response =
                exceptionHandler.handleValidationExceptions(exception);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ApiErrorCode.VALIDATION_ERROR.getCode(), response.getBody().getErrorCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertNotNull(response.getBody().getTimestamp());
        
        Map<String, String> fieldErrors = response.getBody().getFieldErrors();
        assertNotNull(fieldErrors);
        assertEquals(2, fieldErrors.size());
        assertEquals("Name cannot be empty", fieldErrors.get("name"));
        assertEquals("Age must be positive", fieldErrors.get("age"));
    }

    @Test
    @DisplayName("handleNoResourceFoundException should return NOT_FOUND for missing URLs")
    void handleNoResourceFoundExceptionShouldReturnNotFoundForMissingUrls() {
        NoResourceFoundException exception = new NoResourceFoundException(org.springframework.http.HttpMethod.GET, "/invalid/path");
        
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleNoResourceFoundException(exception);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ApiErrorCode.RESOURCE_NOT_FOUND.getCode(), response.getBody().getErrorCode());
        assertEquals("The requested resource was not found", response.getBody().getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatus());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("handleGeneralException should return INTERNAL_SERVER_ERROR for unexpected exceptions")
    void handleGeneralExceptionShouldReturnInternalServerErrorForUnexpectedExceptions() {
        Exception exception = new RuntimeException("Unexpected error");
        
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = exceptionHandler.handleGeneralException(exception);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ApiErrorCode.UNKNOWN_ERROR.getCode(), response.getBody().getErrorCode());
        assertTrue(response.getBody().getMessage().contains("Unexpected error"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getStatus());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("ErrorResponse getters and setters should work correctly")
    void errorResponseGettersAndSettersShouldWorkCorrectly() {
        String errorCode = "TEST-001";
        String message = "Test error message";
        int status = 400;
        LocalDateTime timestamp = LocalDateTime.now();
        
        GlobalExceptionHandler.ErrorResponse errorResponse =
                new GlobalExceptionHandler.ErrorResponse(errorCode, message, status, timestamp);
        
        assertEquals(errorCode, errorResponse.getErrorCode());
        assertEquals(message, errorResponse.getMessage());
        assertEquals(status, errorResponse.getStatus());
        assertEquals(timestamp, errorResponse.getTimestamp());
        
        String newErrorCode = "TEST-002";
        String newMessage = "Updated message";
        int newStatus = 500;
        LocalDateTime newTimestamp = LocalDateTime.now().plusMinutes(5);
        
        errorResponse.setErrorCode(newErrorCode);
        errorResponse.setMessage(newMessage);
        errorResponse.setStatus(newStatus);
        errorResponse.setTimestamp(newTimestamp);
        
        assertEquals(newErrorCode, errorResponse.getErrorCode());
        assertEquals(newMessage, errorResponse.getMessage());
        assertEquals(newStatus, errorResponse.getStatus());
        assertEquals(newTimestamp, errorResponse.getTimestamp());
    }

    @Test
    @DisplayName("ValidationErrorResponse getters and setters should work correctly")
    void validationErrorResponseGettersAndSettersShouldWorkCorrectly() {
        String errorCode = "VALIDATION-001";
        String message = "Validation failed";
        int status = 400;
        LocalDateTime timestamp = LocalDateTime.now();
        Map<String, String> fieldErrors = Map.of("field1", "Error 1", "field2", "Error 2");
        
        GlobalExceptionHandler.ValidationErrorResponse validationResponse =
                new GlobalExceptionHandler.ValidationErrorResponse(
                        errorCode, message, status, timestamp, fieldErrors);
        
        assertEquals(fieldErrors, validationResponse.getFieldErrors());
        assertEquals(2, validationResponse.getFieldErrors().size());
        
        Map<String, String> newFieldErrors = Map.of("field3", "Error 3");
        validationResponse.setFieldErrors(newFieldErrors);
        
        assertEquals(newFieldErrors, validationResponse.getFieldErrors());
        assertEquals(1, validationResponse.getFieldErrors().size());
        
        assertEquals(errorCode, validationResponse.getErrorCode());
        assertEquals(message, validationResponse.getMessage());
        assertEquals(status, validationResponse.getStatus());
        assertEquals(timestamp, validationResponse.getTimestamp());
    }
}
