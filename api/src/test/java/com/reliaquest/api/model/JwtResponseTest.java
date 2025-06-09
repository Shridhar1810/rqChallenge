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
        JwtResponse response = new JwtResponse("test-jwt-token");
        
        assertEquals("test-jwt-token", response.getToken());
    }
    
    @Test
    @DisplayName("JwtResponse can be created with no-args constructor and setters")
    void jwtResponseCanBeCreatedWithNoArgsConstructor() {
        JwtResponse response = new JwtResponse();
        
        response.setToken("test-jwt-token");
        
        assertEquals("test-jwt-token", response.getToken());
    }
    
    @Test
    @DisplayName("JwtResponse implements Serializable interface")
    void jwtResponseImplementsSerializableInterface() {
        JwtResponse response = new JwtResponse("test-jwt-token");
        
        assertTrue(response instanceof java.io.Serializable);
    }
    
    @Test
    @DisplayName("JwtResponse equals method works correctly")
    void jwtResponseEqualsWorksCorrectly() {
        JwtResponse response1 = new JwtResponse("test-token-1");
        JwtResponse response2 = new JwtResponse("test-token-1"); // Same token
        JwtResponse response3 = new JwtResponse("test-token-2"); // Different token
        
        assertEquals(response1, response2); // Same token, should be equal
        assertNotEquals(response1, response3); // Different token, should not be equal
        assertEquals(response1, response1); // Same object, should be equal
        assertNotEquals(response1, null); // Not equal to null
        assertNotEquals(response1, "Not a JwtResponse"); // Not equal to other types
    }
    
    @Test
    @DisplayName("JwtResponse toString includes token field")
    void jwtResponseToStringIncludesTokenField() {
        JwtResponse response = new JwtResponse("test-jwt-token");
        
        String toString = response.toString();
        
        assertTrue(toString.contains("token=test-jwt-token"));
    }
}
