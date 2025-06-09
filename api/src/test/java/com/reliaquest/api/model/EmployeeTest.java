package com.reliaquest.api.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Employee model class
 */
class EmployeeTest {

    @Test
    @DisplayName("Employee can be built with builder pattern")
    void employeeCanBeBuiltWithBuilder() {
        // Arrange & Act
        Employee employee = Employee.builder()
                .id("emp-123")
                .name("John Doe")
                .age(30)
                .salary(50000)
                .title("Software Engineer")
                .build();
        
        // Assert
        assertEquals("emp-123", employee.getId());
        assertEquals("John Doe", employee.getName());
        assertEquals(30, employee.getAge());
        assertEquals(50000, employee.getSalary());
        assertEquals("Software Engineer", employee.getTitle());
    }
    
    @Test
    @DisplayName("Employee can be created with no-args constructor and setters")
    void employeeCanBeCreatedWithNoArgsConstructor() {
        // Arrange
        Employee employee = new Employee();
        
        // Act
        employee.setId("emp-123");
        employee.setName("John Doe");
        employee.setAge(30);
        employee.setSalary(50000);
        employee.setTitle("Software Engineer");
        
        // Assert
        assertEquals("emp-123", employee.getId());
        assertEquals("John Doe", employee.getName());
        assertEquals(30, employee.getAge());
        assertEquals(50000, employee.getSalary());
        assertEquals("Software Engineer", employee.getTitle());
    }
    
    @Test
    @DisplayName("Employee can be created with all-args constructor")
    void employeeCanBeCreatedWithAllArgsConstructor() {
        // Arrange & Act
        Employee employee = new Employee("emp-123", "John Doe", 50000, 30, "Software Engineer");
        
        // Assert
        assertEquals("emp-123", employee.getId());
        assertEquals("John Doe", employee.getName());
        assertEquals(30, employee.getAge());
        assertEquals(50000, employee.getSalary());
        assertEquals("Software Engineer", employee.getTitle());
    }
    
    @Test
    @DisplayName("Employee can be created from EmployeeInput")
    void employeeCanBeCreatedFromEmployeeInput() {
        // Arrange
        EmployeeInput input = new EmployeeInput();
        input.setName("John Doe");
        input.setAge(30);
        input.setSalary(50000);
        input.setTitle("Software Engineer");
        
        // Act
        Employee employee = Employee.fromInput(input);
        
        // Assert
        assertNull(employee.getId()); // ID is not set from input
        assertEquals("John Doe", employee.getName());
        assertEquals(30, employee.getAge());
        assertEquals(50000, employee.getSalary());
        assertEquals("Software Engineer", employee.getTitle());
    }
    
    @Test
    @DisplayName("Employee equals method works correctly")
    void employeeEqualsWorksCorrectly() {
        // Arrange
        Employee employee1 = Employee.builder().id("emp-123").name("John").build();
        Employee employee2 = Employee.builder().id("emp-123").name("Different Name").build();
        Employee employee3 = Employee.builder().id("different-id").name("John").build();
        
        // Act & Assert
        assertEquals(employee1, employee2); // Only ID matters for equality
        assertNotEquals(employee1, employee3); // Different IDs are not equal
        assertNotEquals(employee1, null); // Not equal to null
        assertNotEquals(employee1, new Object()); // Not equal to other types
        assertEquals(employee1, employee1); // Equal to itself
    }
    
    @Test
    @DisplayName("Employee hashCode is based on ID")
    void employeeHashCodeIsBasedOnId() {
        // Arrange
        Employee employee1 = Employee.builder().id("emp-123").name("John").build();
        Employee employee2 = Employee.builder().id("emp-123").name("Different Name").build();
        Employee employee3 = Employee.builder().id("different-id").name("John").build();
        
        // Act & Assert
        assertEquals(employee1.hashCode(), employee2.hashCode()); // Same ID, same hash
        assertNotEquals(employee1.hashCode(), employee3.hashCode()); // Different ID, different hash
    }
    
    @Test
    @DisplayName("Employee toString includes all fields")
    void employeeToStringIncludesAllFields() {
        // Arrange
        Employee employee = Employee.builder()
                .id("emp-123")
                .name("John Doe")
                .age(30)
                .salary(50000)
                .title("Software Engineer")
                .build();
        
        // Act
        String toString = employee.toString();
        
        // Assert
        assertTrue(toString.contains("id=emp-123"));
        assertTrue(toString.contains("name=John Doe"));
        assertTrue(toString.contains("age=30"));
        assertTrue(toString.contains("salary=50000"));
        assertTrue(toString.contains("title=Software Engineer"));
    }
}
