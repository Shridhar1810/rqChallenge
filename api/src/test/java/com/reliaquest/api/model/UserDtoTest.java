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
        UserDto userDto = new UserDto("testuser", "password123");
        
        assertEquals("testuser", userDto.getUsername());
        assertEquals("password123", userDto.getPassword());
    }
    
    @Test
    @DisplayName("UserDto can be created with no-args constructor and setters")
    void userDtoCanBeCreatedWithNoArgsConstructor() {
        UserDto userDto = new UserDto();
        
        userDto.setUsername("testuser");
        userDto.setPassword("password123");
        
        assertEquals("testuser", userDto.getUsername());
        assertEquals("password123", userDto.getPassword());
    }
    
    @Test
    @DisplayName("Valid UserDto passes validation")
    void validUserDtoPassesValidation() {
        UserDto userDto = new UserDto("testuser", "password123");
        
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
        
        assertTrue(violations.isEmpty());
    }
    
    @Test
    @DisplayName("UserDto with blank username fails validation")
    void userDtoWithBlankUsernameFailsValidation() {
        UserDto userDto = new UserDto("", "password123");
        
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
        
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
        UserDto userDto = new UserDto(null, "password123");
        
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
        
        assertEquals(1, violations.size());
        assertEquals("Username cannot be blank", violations.iterator().next().getMessage());
    }
    
    @Test
    @DisplayName("UserDto with username too short fails validation")
    void userDtoWithUsernameTooShortFailsValidation() {
        UserDto userDto = new UserDto("ab", "password123"); // 2 chars, minimum is 3
        
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
        
        assertEquals(1, violations.size());
        assertEquals("Username must be between 3 and 50 characters", violations.iterator().next().getMessage());
    }
    
    @Test
    @DisplayName("UserDto with username too long fails validation")
    void userDtoWithUsernameTooLongFailsValidation() {
        String longUsername = "x".repeat(51); // 51 chars, maximum is 50
        UserDto userDto = new UserDto(longUsername, "password123");
        
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
        
        assertEquals(1, violations.size());
        assertEquals("Username must be between 3 and 50 characters", violations.iterator().next().getMessage());
    }
    
    @Test
    @DisplayName("UserDto with blank password fails validation")
    void userDtoWithBlankPasswordFailsValidation() {
        UserDto userDto = new UserDto("testuser", "");
        
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
        
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
        UserDto userDto = new UserDto("testuser", null);
        
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
        
        assertEquals(1, violations.size());
        assertEquals("Password cannot be blank", violations.iterator().next().getMessage());
    }
    
    @Test
    @DisplayName("UserDto with password too short fails validation")
    void userDtoWithPasswordTooShortFailsValidation() {
        UserDto userDto = new UserDto("testuser", "12345"); // 5 chars, minimum is 6
        
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
        
        assertEquals(1, violations.size());
        assertEquals("Password must be between 6 and 100 characters", violations.iterator().next().getMessage());
    }
    
    @Test
    @DisplayName("UserDto with password too long fails validation")
    void userDtoWithPasswordTooLongFailsValidation() {
        String longPassword = "x".repeat(101); // 101 chars, maximum is 100
        UserDto userDto = new UserDto("testuser", longPassword);
        
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
        
        assertEquals(1, violations.size());
        assertEquals("Password must be between 6 and 100 characters", violations.iterator().next().getMessage());
    }
    
    @Test
    @DisplayName("UserDto with multiple validation failures reports all errors")
    void userDtoWithMultipleValidationFailuresReportsAllErrors() {
        // Arrange - both username and password are invalid
        UserDto userDto = new UserDto("", "");
        
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
        
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
        UserDto userDto = new UserDto("testuser", "password123");
        
        String toString = userDto.toString();
        
        assertTrue(toString.contains("username=testuser"));
        // Only check that the field is included, not its value visibility since @Data doesn't hide password
        assertTrue(toString.contains("password="));
    }
}
