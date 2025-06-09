package com.reliaquest.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter to validate admin API key for sensitive operations
 * Used to protect endpoints like user registration
 */
public class AdminKeyAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AdminKeyAuthFilter.class);
    private static final String API_KEY_HEADER = "Global-Token";
    
    private final RequestMatcher requestMatcher;
    private String adminKey;

    public AdminKeyAuthFilter(RequestMatcher requestMatcher) {
        this.requestMatcher = requestMatcher;
    }

    public void setAdminKey(String adminKey) {
        this.adminKey = adminKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Only check requests that match our path pattern
        if (requestMatcher.matches(request)) {
            String apiKey = request.getHeader(API_KEY_HEADER);
            
            // Check if API key is present and matches
            if (apiKey == null || !apiKey.equals(adminKey)) {
                logger.warn("Unauthorized attempt to access protected endpoint: {}", request.getRequestURI());
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Access denied. Valid API key required.\",\"status\":403}");
                return;
            }
            
            logger.debug("Admin API access authorized for: {}", request.getRequestURI());
        }
        
        filterChain.doFilter(request, response);
    }
}
