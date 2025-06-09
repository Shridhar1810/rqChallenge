package com.reliaquest.api.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the JwtRequest model class
 */
class JwtRequestTest {

    private static Validator validator;
    
    @BeforeAll
    public static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("JwtRequest can be created with all-args constructor")
    void jwtRequestCanBeCreatedWithAllArgsConstructor() {
        // Arrange & Act
        JwtRequest request = new JwtRequest("testuser", "password123");
        
        // Assert
        assertEquals("testuser", request.getUsername());
        assertEquals("password123", request.getPassword());
    }
    
    @Test
    @DisplayName("JwtRequest can be created with no-args constructor and setters")
    void jwtRequestCanBeCreatedWithNoArgsConstructor() {
        // Arrange
        JwtRequest request = new JwtRequest();
        
        // Act
        request.setUsername("testuser");
        request.setPassword("password123");
        
        // Assert
        assertEquals("testuser", request.getUsername());
        assertEquals("password123", request.getPassword());
    }
    
    @Test
    @DisplayName("Valid JwtRequest passes validation")
    void validJwtRequestPassesValidation() {
        // Arrange
        JwtRequest request = new JwtRequest("testuser", "password123");
        
        // Act
        Set<ConstraintViolation<JwtRequest>> violations = validator.validate(request);
        
        // Assert
        assertTrue(violations.isEmpty());
    }
    
    @Test
    @DisplayName("JwtRequest with blank username fails validation")
    void jwtRequestWithBlankUsernameFailsValidation() {
        // Arrange
        JwtRequest request = new JwtRequest("", "password123");
        
        // Act
        Set<ConstraintViolation<JwtRequest>> violations = validator.validate(request);
        
        // Assert
        assertEquals(1, violations.size());
        assertEquals("Username cannot be blank", violations.iterator().next().getMessage());
    }
    
    @Test
    @DisplayName("JwtRequest with null username fails validation")
    void jwtRequestWithNullUsernameFailsValidation() {
        // Arrange
        JwtRequest request = new JwtRequest(null, "password123");
        
        // Act
        Set<ConstraintViolation<JwtRequest>> violations = validator.validate(request);
        
        // Assert
        assertEquals(1, violations.size());
        assertEquals("Username cannot be blank", violations.iterator().next().getMessage());
    }
    
    @Test
    @DisplayName("JwtRequest with blank password fails validation")
    void jwtRequestWithBlankPasswordFailsValidation() {
        // Arrange
        JwtRequest request = new JwtRequest("testuser", "");
        
        // Act
        Set<ConstraintViolation<JwtRequest>> violations = validator.validate(request);
        
        // Assert
        assertEquals(1, violations.size());
        assertEquals("Password cannot be blank", violations.iterator().next().getMessage());
    }
    
    @Test
    @DisplayName("JwtRequest with null password fails validation")
    void jwtRequestWithNullPasswordFailsValidation() {
        // Arrange
        JwtRequest request = new JwtRequest("testuser", null);
        
        // Act
        Set<ConstraintViolation<JwtRequest>> violations = validator.validate(request);
        
        // Assert
        assertEquals(1, violations.size());
        assertEquals("Password cannot be blank", violations.iterator().next().getMessage());
    }
    
    @Test
    @DisplayName("JwtRequest with multiple validation failures reports all errors")
    void jwtRequestWithMultipleValidationFailuresReportsAllErrors() {
        // Arrange
        JwtRequest request = new JwtRequest(null, null);
        
        // Act
        Set<ConstraintViolation<JwtRequest>> violations = validator.validate(request);
        
        // Assert
        assertEquals(2, violations.size());
        
        // Collect the messages and verify both are present
        List<String> messages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(java.util.stream.Collectors.toList());
                
        assertTrue(messages.contains("Username cannot be blank"));
        assertTrue(messages.contains("Password cannot be blank"));
    }
    
    @Test
    @DisplayName("JwtRequest toString includes fields but password may be visible")
    void jwtRequestToStringIncludesAllFields() {
        // Arrange
        JwtRequest request = new JwtRequest("testuser", "password123");
        
        // Act
        String toString = request.toString();
        
        // Assert
        assertTrue(toString.contains("username=testuser"));
        // Only check that the field is included, not its value visibility since @Data doesn't hide password
        assertTrue(toString.contains("password="));
    }
}
