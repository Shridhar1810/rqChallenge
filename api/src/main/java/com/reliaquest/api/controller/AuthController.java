package com.reliaquest.api.controller;

import com.reliaquest.api.model.JwtRequest;
import com.reliaquest.api.model.JwtResponse;
import com.reliaquest.api.model.UserDto;
import com.reliaquest.api.util.JwtTokenUtil;
import com.reliaquest.api.service.JwtUserDetailsService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for authentication operations
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Autowired
    private JwtUserDetailsService userDetailsService;
    
    /**
     * Authenticate user and generate JWT token
     * 
     * @param authRequest the authentication request
     * @return JWT token
     * @throws Exception if authentication fails
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> createAuthenticationToken(@RequestBody JwtRequest authRequest) throws Exception {
        logger.info("Authentication request received for user: {}", authRequest.getUsername());
        
        authenticate(authRequest.getUsername(), authRequest.getPassword());
        
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());
        final String token = jwtTokenUtil.generateToken(userDetails);
        
        logger.info("Successfully authenticated user: {}", authRequest.getUsername());
        return ResponseEntity.ok(new JwtResponse(token));
    }
    
    /**
     * Register a new user
     * 
     * @param userDto user data transfer object
     * @return success message
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDto userDto) {
        logger.info("Registration request received for username: {}", userDto.getUsername());
        
        // Check if user already exists
        try {
            userDetailsService.loadUserByUsername(userDto.getUsername());
            logger.warn("User already exists: {}", userDto.getUsername());
            return ResponseEntity.badRequest().body("User already exists with username: " + userDto.getUsername());
        } catch (Exception ex) {
            // User doesn't exist, so we can proceed with registration
        }
        
        boolean success = userDetailsService.addUser(userDto.getUsername(), userDto.getPassword());
        
        if (success) {
            logger.info("User registered successfully: {}", userDto.getUsername());
            return ResponseEntity.ok("User registered successfully");
        } else {
            logger.error("User registration failed for username: {}", userDto.getUsername());
            return ResponseEntity.badRequest().body("User registration failed");
        }
    }
    
    /**
     * Authenticate user with username and password
     * 
     * @param username the username
     * @param password the password
     * @throws Exception if authentication fails
     */
    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            logger.warn("Invalid credentials for user: {}", username);
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }
}
