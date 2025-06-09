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
 * Unit tests for the EmployeeInput model class, focusing on validation constraints
 */
class EmployeeInputTest {

    private static Validator validator;
    
    @BeforeAll
    public static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("EmployeeInput can be built with builder pattern")
    void employeeInputCanBeBuiltWithBuilder() {
        // Arrange & Act
        EmployeeInput input = EmployeeInput.builder()
                .name("John Doe")
                .age(30)
                .salary(50000)
                .title("Software Engineer")
                .build();
        
        // Assert
        assertEquals("John Doe", input.getName());
        assertEquals(30, input.getAge());
        assertEquals(50000, input.getSalary());
        assertEquals("Software Engineer", input.getTitle());
    }
    
    @Test
    @DisplayName("EmployeeInput can be created with no-args constructor and setters")
    void employeeInputCanBeCreatedWithNoArgsConstructor() {
        // Arrange
        EmployeeInput input = new EmployeeInput();
        
        // Act
        input.setName("John Doe");
        input.setAge(30);
        input.setSalary(50000);
        input.setTitle("Software Engineer");
        
        // Assert
        assertEquals("John Doe", input.getName());
        assertEquals(30, input.getAge());
        assertEquals(50000, input.getSalary());
        assertEquals("Software Engineer", input.getTitle());
    }
    
    @Test
    @DisplayName("EmployeeInput can be created with all-args constructor")
    void employeeInputCanBeCreatedWithAllArgsConstructor() {
        // Arrange & Act
        EmployeeInput input = new EmployeeInput("John Doe", 50000, 30, "Software Engineer");
        
        // Assert
        assertEquals("John Doe", input.getName());
        assertEquals(30, input.getAge());
        assertEquals(50000, input.getSalary());
        assertEquals("Software Engineer", input.getTitle());
    }
    
    @Test
    @DisplayName("Valid EmployeeInput passes validation")
    void validEmployeeInputPassesValidation() {
        // Arrange
        EmployeeInput input = new EmployeeInput("John Doe", 50000, 30, "Software Engineer");
        
        // Act
        Set<ConstraintViolation<EmployeeInput>> violations = validator.validate(input);
        
        // Assert
        assertTrue(violations.isEmpty());
    }
    
    @Test
    @DisplayName("EmployeeInput with blank name fails validation")
    void employeeInputWithBlankNameFailsValidation() {
        // Arrange
        EmployeeInput input = new EmployeeInput("", 50000, 30, "Software Engineer");
        
        // Act
        Set<ConstraintViolation<EmployeeInput>> violations = validator.validate(input);
        
        // Assert
        assertEquals(1, violations.size());
        assertEquals("Name cannot be blank", violations.iterator().next().getMessage());
    }
    
    @Test
    @DisplayName("EmployeeInput with null name fails validation")
    void employeeInputWithNullNameFailsValidation() {
        // Arrange
        EmployeeInput input = new EmployeeInput(null, 50000, 30, "Software Engineer");
        
        // Act
        Set<ConstraintViolation<EmployeeInput>> violations = validator.validate(input);
        
        // Assert
        assertEquals(1, violations.size());
        assertEquals("Name cannot be blank", violations.iterator().next().getMessage());
    }
    
    @Test
    @DisplayName("EmployeeInput with null salary fails validation")
    void employeeInputWithNullSalaryFailsValidation() {
        // Arrange
        EmployeeInput input = new EmployeeInput("John Doe", null, 30, "Software Engineer");
        
        // Act
        Set<ConstraintViolation<EmployeeInput>> violations = validator.validate(input);
        
        // Assert
        assertEquals(1, violations.size());
        assertEquals("Salary must be provided", violations.iterator().next().getMessage());
    }
    
    @Test
    @DisplayName("EmployeeInput with negative salary fails validation")
    void employeeInputWithNegativeSalaryFailsValidation() {
        // Arrange
        EmployeeInput input = new EmployeeInput("John Doe", -5000, 30, "Software Engineer");
        
        // Act
        Set<ConstraintViolation<EmployeeInput>> violations = validator.validate(input);
        
        // Assert
        assertEquals(1, violations.size());
        assertEquals("Salary must be positive", violations.iterator().next().getMessage());
    }
    
    @Test
    @DisplayName("EmployeeInput with null age fails validation")
    void employeeInputWithNullAgeFailsValidation() {
        // Arrange
        EmployeeInput input = new EmployeeInput("John Doe", 50000, null, "Software Engineer");
        
        // Act
        Set<ConstraintViolation<EmployeeInput>> violations = validator.validate(input);
        
        // Assert
        assertEquals(1, violations.size());
        assertEquals("Age must be provided", violations.iterator().next().getMessage());
    }
    
    @Test
    @DisplayName("EmployeeInput with age below minimum fails validation")
    void employeeInputWithAgeBelowMinimumFailsValidation() {
        // Arrange
        EmployeeInput input = new EmployeeInput("John Doe", 50000, 17, "Software Engineer");
        
        // Act
        Set<ConstraintViolation<EmployeeInput>> violations = validator.validate(input);
        
        // Assert
        assertEquals(1, violations.size());
        assertEquals("Age must be at least 18", violations.iterator().next().getMessage());
    }
    
    @Test
    @DisplayName("EmployeeInput with age above maximum fails validation")
    void employeeInputWithAgeAboveMaximumFailsValidation() {
        // Arrange
        EmployeeInput input = new EmployeeInput("John Doe", 50000, 101, "Software Engineer");
        
        // Act
        Set<ConstraintViolation<EmployeeInput>> violations = validator.validate(input);
        
        // Assert
        assertEquals(1, violations.size());
        assertEquals("Age must not exceed 100", violations.iterator().next().getMessage());
    }
    
    @Test
    @DisplayName("EmployeeInput with blank title fails validation")
    void employeeInputWithBlankTitleFailsValidation() {
        // Arrange
        EmployeeInput input = new EmployeeInput("John Doe", 50000, 30, "");
        
        // Act
        Set<ConstraintViolation<EmployeeInput>> violations = validator.validate(input);
        
        // Assert
        assertEquals(1, violations.size());
        assertEquals("Job title cannot be blank", violations.iterator().next().getMessage());
    }
    
    @Test
    @DisplayName("EmployeeInput with null title fails validation")
    void employeeInputWithNullTitleFailsValidation() {
        // Arrange
        EmployeeInput input = new EmployeeInput("John Doe", 50000, 30, null);
        
        // Act
        Set<ConstraintViolation<EmployeeInput>> violations = validator.validate(input);
        
        // Assert
        assertEquals(1, violations.size());
        assertEquals("Job title cannot be blank", violations.iterator().next().getMessage());
    }
    
    @Test
    @DisplayName("EmployeeInput with multiple validation failures reports all errors")
    void employeeInputWithMultipleValidationFailuresReportsAllErrors() {
        // Arrange
        EmployeeInput input = new EmployeeInput("", -5000, 15, "");
        
        // Act
        Set<ConstraintViolation<EmployeeInput>> violations = validator.validate(input);
        
        // Assert
        assertEquals(4, violations.size());
        
        // Collect messages and check for presence of all expected validation messages
        List<String> messages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(java.util.stream.Collectors.toList());
        
        assertTrue(messages.contains("Name cannot be blank"), 
                "Missing validation for blank name");
        assertTrue(messages.contains("Salary must be positive"), 
                "Missing validation for negative salary");
        assertTrue(messages.contains("Age must be at least 18"), 
                "Missing validation for age below minimum");
        assertTrue(messages.contains("Job title cannot be blank"), 
                "Missing validation for blank title");
    }
    
    @Test
    @DisplayName("EmployeeInput toString includes all fields")
    void employeeInputToStringIncludesAllFields() {
        // Arrange
        EmployeeInput input = new EmployeeInput("John Doe", 50000, 30, "Software Engineer");
        
        // Act
        String toString = input.toString();
        
        // Assert
        assertTrue(toString.contains("name=John Doe"));
        assertTrue(toString.contains("age=30"));
        assertTrue(toString.contains("salary=50000"));
        assertTrue(toString.contains("title=Software Engineer"));
    }
}
