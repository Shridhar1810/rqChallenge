package com.reliaquest.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for JwtUserDetailsService
 */
class JwtUserDetailsServiceTest {

    private JwtUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = new JwtUserDetailsService();
    }

    @Test
    @DisplayName("Should load existing user by username")
    void shouldLoadExistingUserByUsername() {
        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("admin");
        
        // Assert
        assertNotNull(userDetails);
        assertEquals("admin", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().isEmpty());
    }

    @Test
    @DisplayName("Should throw exception when loading non-existent user")
    void shouldThrowExceptionWhenLoadingNonExistentUser() {
        // Act & Assert
        Exception exception = assertThrows(UsernameNotFoundException.class, () -> 
                userDetailsService.loadUserByUsername("nonexistent"));
        
        assertTrue(exception.getMessage().contains("User not found with username: nonexistent"));
    }

    @Test
    @DisplayName("Should add new user successfully")
    void shouldAddNewUserSuccessfully() {
        // Arrange
        String username = "newuser";
        String password = "password123";
        
        // Act
        boolean result = userDetailsService.addUser(username, password);
        
        // Assert
        assertTrue(result);
        
        // Verify user was added correctly
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
    }

    @Test
    @DisplayName("Should reject adding duplicate user")
    void shouldRejectAddingDuplicateUser() {
        // Arrange
        String username = "admin"; // already exists in default users
        String password = "newpassword";
        
        // Act
        boolean result = userDetailsService.addUser(username, password);
        
        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Should store passwords using BCrypt")
    void shouldStorePasswordsUsingBCrypt() {
        // Arrange
        String username = "testuser";
        String password = "testpassword";
        
        // Act
        userDetailsService.addUser(username, password);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        
        // Assert
        String encodedPassword = userDetails.getPassword();
        assertTrue(encodedPassword.startsWith("$2a$"));  // BCrypt prefix
        
        // Verify the password matches using BCryptPasswordEncoder
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        assertTrue(encoder.matches(password, encodedPassword));
    }
}
