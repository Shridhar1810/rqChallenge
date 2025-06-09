package com.reliaquest.api.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the JwtTokenUtil class
 */
class JwtTokenUtilTest {

    private JwtTokenUtil jwtTokenUtil;
    private String testSecret;
    private final long testExpiration = 3600000; // 1 hour
    private UserDetails testUserDetails;
    private Key secretKey;

    @BeforeEach
    void setUp() {
        jwtTokenUtil = new JwtTokenUtil();
        
        // Generate a secure key suitable for HS512
        secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        testSecret = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        
        ReflectionTestUtils.setField(jwtTokenUtil, "secret", testSecret);
        ReflectionTestUtils.setField(jwtTokenUtil, "expiration", testExpiration);
        
        testUserDetails = new User("testuser", "password", new ArrayList<>());
    }

    @Test
    @DisplayName("Should generate token with correct username")
    void shouldGenerateTokenWithCorrectUsername() {
        String token = jwtTokenUtil.generateToken(testUserDetails);
        
        assertNotNull(token);
        assertEquals("testuser", jwtTokenUtil.getUsernameFromToken(token));
    }

    @Test
    @DisplayName("Should validate token for same user")
    void shouldValidateTokenForSameUser() {
        String token = jwtTokenUtil.generateToken(testUserDetails);
        
        assertTrue(jwtTokenUtil.validateToken(token, testUserDetails));
    }

    @Test
    @DisplayName("Should not validate token for different user")
    void shouldNotValidateTokenForDifferentUser() {
        String token = jwtTokenUtil.generateToken(testUserDetails);
        UserDetails differentUser = new User("otheruser", "password", new ArrayList<>());
        
        assertFalse(jwtTokenUtil.validateToken(token, differentUser));
    }

    @Test
    @DisplayName("Should not validate expired token")
    void shouldNotValidateExpiredToken() throws Exception {
        ReflectionTestUtils.setField(jwtTokenUtil, "expiration", 1); // 1 ms
        String token = jwtTokenUtil.generateToken(testUserDetails);
        
        // Wait for token to expire
        Thread.sleep(100); // Sleep for 100ms to ensure token expires
        
        try {
            boolean result = jwtTokenUtil.validateToken(token, testUserDetails);
            assertFalse(result);
        } catch (ExpiredJwtException e) {
            // This is also acceptable since the token is definitely expired
            // The method might throw an exception or return false depending on implementation
            assertTrue(true);
        }
    }
    
    @Test
    @DisplayName("Should extract expiration date from token")
    void shouldExtractExpirationDateFromToken() {
        Date now = new Date();
        
        String token = jwtTokenUtil.generateToken(testUserDetails);
        Date expirationDate = jwtTokenUtil.getExpirationDateFromToken(token);
        
        assertNotNull(expirationDate);
        long expectedExpirationTime = now.getTime() + testExpiration;
        
        // Allow for slight variation in timing (within 5 seconds)
        long timeDifference = Math.abs(expectedExpirationTime - expirationDate.getTime());
        assertTrue(timeDifference < 5000);
    }
    
    @Test
    @DisplayName("Should throw ExpiredJwtException when extracting claims from expired token")
    void shouldThrowExceptionWhenExtractingClaimsFromExpiredToken() throws Exception {
        ReflectionTestUtils.setField(jwtTokenUtil, "expiration", 1); // 1 ms
        String token = jwtTokenUtil.generateToken(testUserDetails);
        
        // Wait for token to expire
        Thread.sleep(100);
        
        assertThrows(ExpiredJwtException.class, () -> {
            // Access a protected method using reflection to test getAllClaimsFromToken
            jwtTokenUtil.getClaimFromToken(token, Claims::getSubject);
        });
    }
    
    @Test
    @DisplayName("Should extract custom claim from token")
    void shouldExtractCustomClaimFromToken() {
        // Create token with custom claims using the same key as the jwtTokenUtil
        Map<String, Object> customClaims = new HashMap<>();
        customClaims.put("role", "ADMIN");
        customClaims.put("userId", 123);
        
        // Use the actual JwtTokenUtil instance to generate the token
        // For custom claims, we need to access the private method indirectly
        Map<String, Object> claims = new HashMap<>(customClaims);
        
        // Use reflection to call the private createToken method
        String token = callCreateTokenMethod(claims, testUserDetails.getUsername());
        
        // Extract custom claim and verify - we need to add fields to the normal JWT 
        // which we can do by parsing the token and adding the claims
        String role = jwtTokenUtil.getClaimFromToken(token, claimsObj -> claimsObj.get("role", String.class));
        Integer userId = jwtTokenUtil.getClaimFromToken(token, claimsObj -> claimsObj.get("userId", Integer.class));
        
        assertEquals("ADMIN", role);
        assertEquals(123, userId);
    }
    
    /**
     * Helper method to call the private createToken method via reflection
     */
    private String callCreateTokenMethod(Map<String, Object> claims, String subject) {
        try {
            // Get the createToken method using reflection
            java.lang.reflect.Method createTokenMethod = JwtTokenUtil.class.getDeclaredMethod(
                    "createToken", Map.class, String.class);
            createTokenMethod.setAccessible(true);
            
            // Call the method on our jwtTokenUtil instance
            return (String) createTokenMethod.invoke(jwtTokenUtil, claims, subject);
        } catch (Exception e) {
            fail("Failed to call createToken method via reflection: " + e.getMessage());
            return null;
        }
    }
    
    @Test
    @DisplayName("Should handle malformed JWT token")
    void shouldHandleMalformedJwtToken() {
        String invalidToken = "invalid.token.format";
        
        Exception exception = assertThrows(Exception.class, () -> {
            jwtTokenUtil.getUsernameFromToken(invalidToken);
        });
        
        assertTrue(exception.getMessage().contains("JWT"));
    }
    
    @Test
    @DisplayName("Should detect token manipulations")
    void shouldDetectTokenManipulations() {
        // Arrange - generate valid token
        String validToken = jwtTokenUtil.generateToken(testUserDetails);
        
        // Act - tamper with the token by changing a character in the payload (second) part
        String[] parts = validToken.split("\\.");
        // Need to modify the middle part (payload) - decode, modify, encode
        String decodedPayload = new String(Base64.getUrlDecoder().decode(parts[1]));
        // Modify the payload by replacing the username
        String tamperedPayload = decodedPayload.replace("testuser", "hacker12");
        // Re-encode
        String encodedTamperedPayload = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(tamperedPayload.getBytes());
        
        String tamperedToken = parts[0] + "." + encodedTamperedPayload + "." + parts[2];
        
        // Assert - validation should fail with signature exception when token is manipulated
        assertThrows(io.jsonwebtoken.security.SignatureException.class, () -> {
            jwtTokenUtil.validateToken(tamperedToken, testUserDetails);
        });
    }
}
