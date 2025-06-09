package com.reliaquest.api.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Service to load user-specific data for authentication
 */
@Service
public class JwtUserDetailsService implements UserDetailsService {

    private final Map<String, String> users = new HashMap<>();
    private final BCryptPasswordEncoder passwordEncoder;
    
    public JwtUserDetailsService() {
        this.passwordEncoder = new BCryptPasswordEncoder();
        // Adding some default users for demonstration
        users.put("admin", passwordEncoder.encode("admin"));
        users.put("user", passwordEncoder.encode("user123"));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String encryptedPassword = users.get(username);
        if (encryptedPassword == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        
        return new User(username, encryptedPassword, new ArrayList<>());
    }
    
    /**
     * Add a new user to the system
     * 
     * @param username the username
     * @param password the raw password (will be encrypted)
     * @return true if user was added successfully
     */
    public boolean addUser(String username, String password) {
        if (users.containsKey(username)) {
            return false; // User already exists
        }
        users.put(username, passwordEncoder.encode(password));
        return true;
    }
}
