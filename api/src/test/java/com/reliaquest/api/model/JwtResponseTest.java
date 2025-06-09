package com.reliaquest.api.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the JwtResponse model class
 */
class JwtResponseTest {

    @Test
    @DisplayName("JwtResponse can be created with all-args constructor")
    void jwtResponseCanBeCreatedWithAllArgsConstructor() {
        // Arrange & Act
        JwtResponse response = new JwtResponse("test-jwt-token");
        
        // Assert
        assertEquals("test-jwt-token", response.getToken());
    }
    
    @Test
    @DisplayName("JwtResponse can be created with no-args constructor and setters")
    void jwtResponseCanBeCreatedWithNoArgsConstructor() {
        // Arrange
        JwtResponse response = new JwtResponse();
        
        // Act
        response.setToken("test-jwt-token");
        
        // Assert
        assertEquals("test-jwt-token", response.getToken());
    }
    
    @Test
    @DisplayName("JwtResponse implements Serializable interface")
    void jwtResponseImplementsSerializableInterface() {
        // Arrange
        JwtResponse response = new JwtResponse("test-jwt-token");
        
        // Act & Assert
        assertTrue(response instanceof java.io.Serializable);
    }
    
    @Test
    @DisplayName("JwtResponse equals method works correctly")
    void jwtResponseEqualsWorksCorrectly() {
        // Arrange
        JwtResponse response1 = new JwtResponse("test-token-1");
        JwtResponse response2 = new JwtResponse("test-token-1"); // Same token
        JwtResponse response3 = new JwtResponse("test-token-2"); // Different token
        
        // Act & Assert
        assertEquals(response1, response2); // Same token, should be equal
        assertNotEquals(response1, response3); // Different token, should not be equal
        assertEquals(response1, response1); // Same object, should be equal
        assertNotEquals(response1, null); // Not equal to null
        assertNotEquals(response1, "Not a JwtResponse"); // Not equal to other types
    }
    
    @Test
    @DisplayName("JwtResponse toString includes token field")
    void jwtResponseToStringIncludesTokenField() {
        // Arrange
        JwtResponse response = new JwtResponse("test-jwt-token");
        
        // Act
        String toString = response.toString();
        
        // Assert
        assertTrue(toString.contains("token=test-jwt-token"));
    }
}
