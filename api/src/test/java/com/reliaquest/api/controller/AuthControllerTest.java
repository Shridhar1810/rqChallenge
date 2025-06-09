package com.reliaquest.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.model.JwtRequest;
import com.reliaquest.api.model.UserDto;
import com.reliaquest.api.util.JwtTokenUtil;
import com.reliaquest.api.service.JwtUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.ArrayList;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @MockBean
    private JwtUserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Value("${api.admin.key:adminSecretKey123}")
    private String adminKey;

    private JwtRequest loginRequest;
    private UserDto registrationRequest;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        loginRequest = new JwtRequest("testuser", "password123");
        registrationRequest = new UserDto("newuser", "newpassword123");
        userDetails = new User("testuser", "password123", new ArrayList<>());
    }

    @Test
    @DisplayName("Login - should return JWT token when credentials are valid")
    void createAuthenticationToken_ShouldReturnJwtToken() throws Exception {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userDetailsService.loadUserByUsername(loginRequest.getUsername())).thenReturn(userDetails);
        when(jwtTokenUtil.generateToken(userDetails)).thenReturn("test-jwt-token");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("test-jwt-token")));

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService, times(1)).loadUserByUsername(loginRequest.getUsername());
        verify(jwtTokenUtil, times(1)).generateToken(userDetails);
    }
    
    @Test
    @DisplayName("Login - should return 500 when credentials are invalid")
    void createAuthenticationToken_ShouldReturnErrorWhenCredentialsInvalid() throws Exception {
        // Given
        BadCredentialsException badCredentialsException = new BadCredentialsException("Invalid credentials");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(badCredentialsException);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", is("An unexpected error occurred: INVALID_CREDENTIALS")));

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtTokenUtil, never()).generateToken(any(UserDetails.class));
    }
    
    @Test
    @DisplayName("Login - should return 500 when user is disabled")
    void createAuthenticationToken_ShouldReturnErrorWhenUserDisabled() throws Exception {
        // Given
        DisabledException disabledException = new DisabledException("User is disabled");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(disabledException);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", is("An unexpected error occurred: USER_DISABLED")));

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtTokenUtil, never()).generateToken(any(UserDetails.class));
    }

    @Test
    @DisplayName("Registration - should register new user when not existing and return success")
    void registerUser_WhenUserDoesNotExist_ShouldRegisterAndReturnSuccess() throws Exception {
        // Given
        when(userDetailsService.loadUserByUsername(registrationRequest.getUsername()))
                .thenThrow(new RuntimeException("User not found"));
        when(userDetailsService.addUser(anyString(), anyString())).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                .header("Global-Token", adminKey) // Add required admin key header
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is("User registered successfully")));

        verify(userDetailsService, times(1)).loadUserByUsername(registrationRequest.getUsername());
        verify(userDetailsService, times(1)).addUser(registrationRequest.getUsername(), registrationRequest.getPassword());
    }

    @Test
    @DisplayName("Registration - should return error when user already exists")
    void registerUser_WhenUserAlreadyExists_ShouldReturnError() throws Exception {
        // Given
        when(userDetailsService.loadUserByUsername(registrationRequest.getUsername()))
                .thenReturn(userDetails);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                .header("Global-Token", adminKey) // Add required admin key header
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", is("User already exists with username: " + registrationRequest.getUsername())));

        verify(userDetailsService, times(1)).loadUserByUsername(registrationRequest.getUsername());
        verify(userDetailsService, never()).addUser(anyString(), anyString());
    }
    
    @Test
    @DisplayName("Registration - should return forbidden when admin key is missing")
    void registerUser_WithoutAdminKey_ShouldReturnForbidden() throws Exception {
        // When & Then - No admin key header provided
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.status", is(403)));
                
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(userDetailsService, never()).addUser(anyString(), anyString());
    }
    
    @Test
    @DisplayName("Registration - should return forbidden when admin key is invalid")
    void registerUser_WithInvalidAdminKey_ShouldReturnForbidden() throws Exception {
        // When & Then - Wrong admin key provided
        mockMvc.perform(post("/api/v1/auth/register")
                .header("Global-Token", "wrong-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.status", is(403)));
                
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(userDetailsService, never()).addUser(anyString(), anyString());
    }
    
    @Test
    @DisplayName("Registration - should return bad request when registration fails")
    void registerUser_WhenRegistrationFails_ShouldReturnBadRequest() throws Exception {
        // Given
        when(userDetailsService.loadUserByUsername(registrationRequest.getUsername()))
                .thenThrow(new RuntimeException("User not found"));
        when(userDetailsService.addUser(anyString(), anyString())).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                .header("Global-Token", adminKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", is("User registration failed")));

        verify(userDetailsService, times(1)).loadUserByUsername(registrationRequest.getUsername());
        verify(userDetailsService, times(1)).addUser(registrationRequest.getUsername(), registrationRequest.getPassword());
    }
    
    @Test
    @DisplayName("Registration - should validate request body")
    void registerUser_WithInvalidInput_ShouldReturnBadRequest() throws Exception {
        // Given - invalid input with empty username and password
        UserDto invalidUserDto = new UserDto("", "");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                .header("Global-Token", adminKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUserDto)))
                .andExpect(status().isBadRequest());
                
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(userDetailsService, never()).addUser(anyString(), anyString());
    }
}
