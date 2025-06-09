package com.reliaquest.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Configuration for RestTemplate with logging and error handling
 */
@Configuration
public class RestTemplateConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(RestTemplateConfig.class);
    
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        // Create a request factory that supports logging both request and response bodies
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        
        // Configure timeouts
        requestFactory.setConnectTimeout(5000); // 5 seconds
        requestFactory.setReadTimeout(10000);   // 10 seconds
        
        return builder
                // BufferingClientHttpRequestFactory allows the response body to be read multiple times
                // which is necessary for our logging interceptor
                .requestFactory(() -> new BufferingClientHttpRequestFactory(requestFactory))
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .interceptors(Collections.singletonList(new LoggingInterceptor()))
                .build();
    }
    
    /**
     * Intercepts HTTP requests and responses for logging
     */
    public class LoggingInterceptor implements ClientHttpRequestInterceptor {
        
        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, 
                                          ClientHttpRequestExecution execution) throws IOException {
            
            // Log request
            logger.debug("Request: {} {}", request.getMethod(), request.getURI());
            logger.debug("Request headers: {}", request.getHeaders());
            logger.debug("Request body: {}", new String(body, StandardCharsets.UTF_8));
            
            // Execute request and get response
            ClientHttpResponse response = execution.execute(request, body);
            
            // Log response
            logger.debug("Response status: {}", response.getStatusCode());
            logger.debug("Response headers: {}", response.getHeaders());
            
            // Log response body if debug is enabled
            if (logger.isDebugEnabled()) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.getBody(), StandardCharsets.UTF_8));
                String responseBody = reader.lines().collect(Collectors.joining("\n"));
                logger.debug("Response body: {}", responseBody);
            }
            
            return response;
        }
    }
}
