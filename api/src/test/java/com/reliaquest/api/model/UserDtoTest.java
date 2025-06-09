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
 * Unit tests for the UserDto model class
 */
class UserDtoTest {

    private static Validator validator;
    
    @BeforeAll
    public static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("UserDto can be created with all-args constructor")
    void userDtoCanBeCreatedWithAllArgsConstructor() {
        // Arrange & Act
        UserDto userDto = new UserDto("testuser", "password123");
        
        // Assert
        assertEquals("testuser", userDto.getUsername());
        assertEquals("password123", userDto.getPassword());
    }
    
    @Test
    @DisplayName("UserDto can be created with no-args constructor and setters")
    void userDtoCanBeCreatedWithNoArgsConstructor() {
        // Arrange
        UserDto userDto = new UserDto();
        
        // Act
        userDto.setUsername("testuser");
        userDto.setPassword("password123");
        
        // Assert
        assertEquals("testuser", userDto.getUsername());
        assertEquals("password123", userDto.getPassword());
    }
    
    @Test
    @DisplayName("Valid UserDto passes validation")
    void validUserDtoPassesValidation() {
        // Arrange
        UserDto userDto = new UserDto("testuser", "password123");
        
        // Act
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
        
        // Assert
        assertTrue(violations.isEmpty());
    }
    
    @Test
    @DisplayName("UserDto with blank username fails validation")
    void userDtoWithBlankUsernameFailsValidation() {
        // Arrange
        UserDto userDto = new UserDto("", "password123");
        
        // Act
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
        
        // Assert
        // There may be multiple validation failures since blank fields can also fail size constraints
        assertTrue(violations.size() >= 1);
        
        // Verify the NotBlank constraint was triggered
        boolean hasNotBlankViolation = violations.stream()
                .map(ConstraintViolation::getMessage)
                .anyMatch(message -> message.equals("Username cannot be blank"));
                
        assertTrue(hasNotBlankViolation, "Should have 'Username cannot be blank' violation");
    }
    
    @Test
    @DisplayName("UserDto with null username fails validation")
    void userDtoWithNullUsernameFailsValidation() {
        // Arrange
        UserDto userDto = new UserDto(null, "password123");
        
        // Act
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
        
        // Assert
        assertEquals(1, violations.size());
        assertEquals("Username cannot be blank", violations.iterator().next().getMessage());
    }
    
    @Test
    @DisplayName("UserDto with username too short fails validation")
    void userDtoWithUsernameTooShortFailsValidation() {
        // Arrange
        UserDto userDto = new UserDto("ab", "password123"); // 2 chars, minimum is 3
        
        // Act
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
        
        // Assert
        assertEquals(1, violations.size());
        assertEquals("Username must be between 3 and 50 characters", violations.iterator().next().getMessage());
    }
    
    @Test
    @DisplayName("UserDto with username too long fails validation")
    void userDtoWithUsernameTooLongFailsValidation() {
        // Arrange
        String longUsername = "x".repeat(51); // 51 chars, maximum is 50
        UserDto userDto = new UserDto(longUsername, "password123");
        
        // Act
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
        
        // Assert
        assertEquals(1, violations.size());
        assertEquals("Username must be between 3 and 50 characters", violations.iterator().next().getMessage());
    }
    
    @Test
    @DisplayName("UserDto with blank password fails validation")
    void userDtoWithBlankPasswordFailsValidation() {
        // Arrange
        UserDto userDto = new UserDto("testuser", "");
        
        // Act
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
        
        // Assert
        // There may be multiple validation failures since blank fields can also fail size constraints
        assertTrue(violations.size() >= 1);
        
        // Verify the NotBlank constraint was triggered
        boolean hasNotBlankViolation = violations.stream()
                .map(ConstraintViolation::getMessage)
                .anyMatch(message -> message.equals("Password cannot be blank"));
                
        assertTrue(hasNotBlankViolation, "Should have 'Password cannot be blank' violation");
    }
    
    @Test
    @DisplayName("UserDto with null password fails validation")
    void userDtoWithNullPasswordFailsValidation() {
        // Arrange
        UserDto userDto = new UserDto("testuser", null);
        
        // Act
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
        
        // Assert
        assertEquals(1, violations.size());
        assertEquals("Password cannot be blank", violations.iterator().next().getMessage());
    }
    
    @Test
    @DisplayName("UserDto with password too short fails validation")
    void userDtoWithPasswordTooShortFailsValidation() {
        // Arrange
        UserDto userDto = new UserDto("testuser", "12345"); // 5 chars, minimum is 6
        
        // Act
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
        
        // Assert
        assertEquals(1, violations.size());
        assertEquals("Password must be between 6 and 100 characters", violations.iterator().next().getMessage());
    }
    
    @Test
    @DisplayName("UserDto with password too long fails validation")
    void userDtoWithPasswordTooLongFailsValidation() {
        // Arrange
        String longPassword = "x".repeat(101); // 101 chars, maximum is 100
        UserDto userDto = new UserDto("testuser", longPassword);
        
        // Act
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
        
        // Assert
        assertEquals(1, violations.size());
        assertEquals("Password must be between 6 and 100 characters", violations.iterator().next().getMessage());
    }
    
    @Test
    @DisplayName("UserDto with multiple validation failures reports all errors")
    void userDtoWithMultipleValidationFailuresReportsAllErrors() {
        // Arrange - both username and password are invalid
        UserDto userDto = new UserDto("", "");
        
        // Act
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
        
        // Assert
        // There may be multiple validation failures per field since blank fields can also fail size constraints
        assertTrue(violations.size() >= 2);
        
        // Collect all messages to verify presence of key validation errors
        List<String> messages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(java.util.stream.Collectors.toList());
        
        // Verify key constraints were triggered        
        assertTrue(messages.contains("Username cannot be blank"), "Should include username blank violation");
        assertTrue(messages.contains("Password cannot be blank"), "Should include password blank violation");
    }
    
    @Test
    @DisplayName("UserDto toString includes username and password fields")
    void userDtoToStringIncludesFields() {
        // Arrange
        UserDto userDto = new UserDto("testuser", "password123");
        
        // Act
        String toString = userDto.toString();
        
        // Assert
        assertTrue(toString.contains("username=testuser"));
        // Only check that the field is included, not its value visibility since @Data doesn't hide password
        assertTrue(toString.contains("password="));
    }
}
