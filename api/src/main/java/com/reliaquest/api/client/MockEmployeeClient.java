package com.reliaquest.api.client;

import com.reliaquest.api.exception.ApiErrorCode;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.exception.ExternalApiException;
import com.reliaquest.api.exception.RateLimitedException;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Client service for interacting with the Mock Employee API
 * Uses Java 8 features like Optional, Stream API, and Lambda expressions
 */
@Component
public class MockEmployeeClient {
    
    private static final Logger logger = LoggerFactory.getLogger(MockEmployeeClient.class);
    
    private final RestTemplate restTemplate;
    private final String baseUrl;
    
    /**
     * Constructor with dependency injection
     * 
     * @param restTemplate REST client for API calls
     * @param baseUrl base URL for the mock API
     */
    public MockEmployeeClient(
            RestTemplate restTemplate,
            @Value("${mock.api.base-url:http://localhost:8112/api/v1/employee}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        logger.info("Initialized Mock Employee Client with base URL: {}", baseUrl);
    }
    
    /**
     * Get all employees from the mock API
     * Uses retry mechanism to handle potential rate limiting
     * 
     * @return list of employees
     * @throws ExternalApiException if the API call fails after retries
     */
    @Retryable(
        value = {HttpServerErrorException.class, HttpClientErrorException.TooManyRequests.class, ResourceAccessException.class},
        maxAttempts = 2,
        backoff = @Backoff(delay = 2000, multiplier = 2, maxDelay = 10000)
    )
    public List<Employee> getAllEmployees() {
        try {
            logger.info("Attempting to fetch all employees from mock API at URL: {}", baseUrl);
            
            // Add headers for better debugging
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            // Make the API call with explicit headers
            ResponseEntity<MockApiResponse<List<Map<String, Object>>>> response = restTemplate.exchange(
                    baseUrl,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<MockApiResponse<List<Map<String, Object>>>>() {}
            );
            
            logger.info("Received response with status: {}", response.getStatusCode());
            
            if (response.getBody() == null) {
                throw new ExternalApiException(ApiErrorCode.EXTERNAL_API_ERROR, "Empty response from mock API");
            }
            
            // Debug the entire response structure
            logger.debug("Employee data from API - Status: {}, Data: {}",
                    response.getBody().getStatus(),
                    response.getBody().getData() != null ? 
                         "Found " + response.getBody().getData().size() + " employees" : "null");
            
            return Optional.ofNullable(response.getBody().getData())
                    .orElseThrow(() -> new ExternalApiException(ApiErrorCode.EXTERNAL_API_ERROR, "Empty data in response"))
                    .stream()
                    .map(this::mapToEmployee)
                    .collect(Collectors.toList());
            
        } catch (HttpClientErrorException.TooManyRequests e) {
            int retryAfter = getRetryAfterSeconds(e);
            logger.warn("Rate limit exceeded when fetching employees. Retry after {} seconds", retryAfter);
            throw new RateLimitedException("Too many requests. Please try again later.", e, retryAfter);
        } catch (HttpClientErrorException ex) {
            logger.error("Client error fetching employees: {} - {}", ex.getStatusCode(), ex.getMessage());
            throw new ExternalApiException(
                ApiErrorCode.EXTERNAL_API_ERROR, 
                "An error occurred while processing your request. Please try again later.",
                ex
            );
        } catch (HttpServerErrorException ex) {
            logger.error("Server error fetching employees: {} - {}", ex.getStatusCode(), ex.getMessage());
            throw new ExternalApiException(
                ApiErrorCode.EXTERNAL_API_UNAVAILABLE, 
                "The service is temporarily unavailable. Please try again later.",
                ex
            );
        } catch (ResourceAccessException ex) {
            logger.error("Resource access error fetching employees: {}", ex.getMessage());
            throw new ExternalApiException(
                ApiErrorCode.EXTERNAL_API_UNAVAILABLE, 
                "The service is temporarily unavailable. Please try again later.",
                ex
            );
        } catch (Exception ex) {
            logger.error("Unexpected error fetching employees: {}", ex.getMessage(), ex);
            throw new ExternalApiException(
                ApiErrorCode.UNKNOWN_ERROR, 
                "An unexpected error occurred. Please try again later.",
                ex
            );
        }
    }

    /**
     * Get retry-after value from response headers or default
     * 
     * @param ex HTTP client exception 
     * @return seconds to wait before retry
     */
    private int getRetryAfterSeconds(HttpClientErrorException ex) {
        try {
            // Try to get Retry-After header
            String retryAfter = ex.getResponseHeaders().getFirst("Retry-After");
            if (retryAfter != null) {
                return Integer.parseInt(retryAfter);
            }
        } catch (Exception e) {
            logger.warn("Failed to parse Retry-After header", e);
        }
        
        // Default retry after 5 seconds
        return 5;
    }
    
    /**
     * Get employee by ID from the mock API
     * 
     * @param id employee ID
     * @return employee
     * @throws ExternalApiException if the API call fails
     */
    @Retryable(
        value = {HttpServerErrorException.class, HttpClientErrorException.TooManyRequests.class, ResourceAccessException.class},
        maxAttempts = 2,
        backoff = @Backoff(delay = 2000, multiplier = 2, maxDelay = 10000)
    )
    public Employee getEmployeeById(String id) {
        try {
            logger.debug("Fetching employee with ID: {}", id);
            
            // Add headers for better debugging
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<MockApiResponse<Map<String, Object>>> response = restTemplate.exchange(
                    baseUrl + "/" + id,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<MockApiResponse<Map<String, Object>>>() {}
            );
            
            if (response.getBody() == null) {
                throw new ExternalApiException(ApiErrorCode.EXTERNAL_API_ERROR, "Empty response from mock API");
            }
            
            // Debug the response
            logger.debug("Employee data from API - Status: {}, Employee details: {}",
                    response.getBody().getStatus(),
                    response.getBody().getData() != null ? 
                         response.getBody().getData() : "null");
            
            Map<String, Object> employeeData = Optional.ofNullable(response.getBody().getData())
                    .orElseThrow(() -> new ExternalApiException(ApiErrorCode.RESOURCE_NOT_FOUND, 
                                 "Employee not found with ID: " + id));
            
            return mapToEmployee(employeeData);
            
        } catch (HttpClientErrorException.TooManyRequests e) {
            int retryAfter = getRetryAfterSeconds(e);
            logger.warn("Rate limit exceeded when fetching employee with ID: {}. Retry after {} seconds", id, retryAfter);
            throw new RateLimitedException("Too many requests. Please try again later.", e, retryAfter);
        } catch (HttpClientErrorException.NotFound ex) {
            logger.error("Employee not found with ID: {}", id);
            throw new EmployeeNotFoundException("Employee not found with ID: " + id);
        } catch (HttpClientErrorException ex) {
            // Log the detailed error for debugging
            logger.error("Client error fetching employee: {} - {}", ex.getStatusCode(), ex.getMessage());
            
            // Return a user-friendly message
            throw new ExternalApiException(
                ApiErrorCode.EXTERNAL_API_ERROR, 
                "Unable to process your request at this time. Please try again later.",
                ex
            );
        } catch (HttpServerErrorException ex) {
            // Log the detailed error for debugging
            logger.error("Server error fetching employee: {} - {}", ex.getStatusCode(), ex.getMessage());
            
            // Return a user-friendly message
            throw new ExternalApiException(
                ApiErrorCode.EXTERNAL_API_UNAVAILABLE, 
                "The service is temporarily unavailable. Please try again later.",
                ex
            );
        } catch (ResourceAccessException ex) {
            // Log the detailed error for debugging
            logger.error("Resource access error fetching employee: {}", ex.getMessage());
            
            // Return a user-friendly message
            throw new ExternalApiException(
                ApiErrorCode.EXTERNAL_API_UNAVAILABLE, 
                "The service is temporarily unavailable. Please try again later.",
                ex
            );
        } catch (Exception ex) {
            logger.error("Unexpected error fetching employee: {}", ex.getMessage(), ex);
            throw new ExternalApiException(
                ApiErrorCode.UNKNOWN_ERROR, 
                "Unexpected error when fetching employee: " + ex.getMessage(), 
                ex
            );
        }
    }
    
    /**
     * Create a new employee via the mock API
     * 
     * @param input employee input data
     * @return created employee
     * @throws ExternalApiException if the API call fails
     */
    public Employee createEmployee(EmployeeInput input) {
        try {
            logger.debug("Creating employee: {}", input);
            
            // Map EmployeeInput to the format expected by the mock API
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("name", input.getName());
            requestBody.put("salary", input.getSalary());
            requestBody.put("age", input.getAge());
            
            requestBody.put("title", input.getTitle());
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<MockApiResponse<Map<String, Object>>> response = restTemplate.exchange(
                    baseUrl,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<MockApiResponse<Map<String, Object>>>() {}
            );
            
            if (response.getBody() == null) {
                throw new ExternalApiException(ApiErrorCode.EXTERNAL_API_ERROR, "Empty response from mock API");
            }
            
            // Debug the response
            logger.debug("Raw employee response data from API - Status: {}, Employee details: {}", 
                    response.getBody().getStatus(),
                    response.getBody().getData() != null ? 
                         response.getBody().getData() : "null");
            
            return mapToEmployee(response.getBody().getData());
            
        } catch (ResourceAccessException ex) {
            if (ex.getCause() instanceof java.net.SocketTimeoutException) {
                logger.warn("Timeout when creating employee: {}", ex.getMessage());
                throw new ExternalApiException(
                    ApiErrorCode.EXTERNAL_API_TIMEOUT, 
                    "Request timed out. Please try again later.", 
                    ex
                );
            }
            // Log the detailed error for debugging purposes
            logger.error("Resource access error creating employee: {}", ex.getMessage());
            
            // Return a user-friendly message without exposing implementation details
            throw new ExternalApiException(
                ApiErrorCode.EXTERNAL_API_UNAVAILABLE, 
                "The service is temporarily unavailable. Please try again later.", 
                ex
            );
        } catch (HttpClientErrorException ex) {
            // Log the detailed error for debugging
            logger.error("Client error creating employee: {} - {}", ex.getStatusCode(), ex.getMessage());
            
            // Return a user-friendly message
            if (ex.getStatusCode().value() == 429) {
                int retryAfter = getRetryAfterSeconds(ex);
                throw new RateLimitedException(
                    "Too many requests. Please try again later.", 
                    ex, 
                    retryAfter
                );
            } else {
                throw new ExternalApiException(
                    ApiErrorCode.EXTERNAL_API_ERROR, 
                    "Unable to process your request at this time. Please try again later.", 
                    ex
                );
            }
        } catch (HttpServerErrorException ex) {
            // Log the detailed error for debugging
            logger.error("Server error creating employee: {} - {}", ex.getStatusCode(), ex.getMessage());
            
            // Return a user-friendly message
            throw new ExternalApiException(
                ApiErrorCode.EXTERNAL_API_UNAVAILABLE, 
                "The service is temporarily unavailable. Please try again later.", 
                ex
            );
        } catch (Exception ex) {
            logger.error("Unexpected error creating employee: {}", ex.getMessage(), ex);
            throw new ExternalApiException(
                ApiErrorCode.UNKNOWN_ERROR, 
                "Unexpected error when creating employee: " + ex.getMessage(), 
                ex
            );
        }
    }
    
    /**
     * Delete an employee by ID via the mock API
     * 
     * @param id employee ID
     * @return true if deleted successfully
     * @throws ExternalApiException if the API call fails
     */
    public boolean deleteEmployee(String id) {
        try {
            logger.debug("Deleting employee with ID: {}", id);
            
            // For the mock API, we need to get the employee first to use the name for deletion
            Employee employee = getEmployeeById(id);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("name", employee.getName());
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<MockApiResponse<Boolean>> response = restTemplate.exchange(
                    baseUrl,
                    HttpMethod.DELETE,
                    entity,
                    new ParameterizedTypeReference<MockApiResponse<Boolean>>() {}
            );
            
            return Optional.ofNullable(response.getBody())
                    .map(MockApiResponse::getData)
                    .orElseThrow(() -> new ExternalApiException(ApiErrorCode.EXTERNAL_API_ERROR, "Empty response from mock API"));
            
        } catch (HttpClientErrorException.TooManyRequests e) {
            logger.warn("Rate limit exceeded when deleting employee (will retry): {}", e.getMessage());
            throw e;
        } catch (HttpClientErrorException ex) {
            logger.error("Client error deleting employee: {}", ex.getMessage());
            throw new ExternalApiException(
                ApiErrorCode.EXTERNAL_API_ERROR, 
                "Unable to process your request at this time. Please try again later.",
                ex
            );
        } catch (HttpServerErrorException ex) {
            logger.error("Server error deleting employee: {}", ex.getMessage());
            throw new ExternalApiException(
                ApiErrorCode.EXTERNAL_API_UNAVAILABLE, 
                "The service is temporarily unavailable. Please try again later.",
                ex
            );
        } catch (ResourceAccessException ex) {
            logger.error("Resource access error deleting employee: {}", ex.getMessage());
            throw new ExternalApiException(
                ApiErrorCode.EXTERNAL_API_UNAVAILABLE, 
                "The service is temporarily unavailable. Please try again later.",
                ex
            );
        } catch (Exception ex) {
            logger.error("Unexpected error deleting employee: {}", ex.getMessage());
            throw new ExternalApiException(
                ApiErrorCode.UNKNOWN_ERROR, 
                "Unexpected error when deleting employee",
                ex
            );
        }
    }
    
    /**
     * Map the mock API response to our Employee model
     * Uses Java Optional for safe conversion of values
     * 
     * @param data raw employee data
     * @return mapped Employee object
     */
    private Employee mapToEmployee(Map<String, Object> data) {
        if (data == null) {
            throw new ExternalApiException(ApiErrorCode.EXTERNAL_API_ERROR, "Invalid employee data received from API");
        }

        try {
            logger.debug("Mapping employee data: {}", data);
            
            Employee employee = new Employee();
            
            // ID field (common across APIs)
            employee.setId(getStringValue(data, "id"));
            
            // Name field - check both employee_name (older APIs) and name (newer APIs)
            String name = getStringValue(data, "employee_name");
            employee.setName(name);
            
            // Salary field - check both employee_salary and salary
            Integer salary = getIntegerValue(data, "employee_salary");
            employee.setSalary(salary);
            
            // Age field - check both employee_age and age
            Integer age = getIntegerValue(data, "employee_age");
            employee.setAge(age);
            
            // Job title field - check title, job_title and employee_title
            // Note: The mock API uses "title" instead of "jobTitle" for job title
            String title = getStringValue(data, "employee_title");
            employee.setTitle(title);
            
            return employee;
        } catch (Exception e) {
            logger.error("Failed to map employee data: {} - Exception: {}", data, e.getMessage(), e);
            throw new ExternalApiException(
                ApiErrorCode.EXTERNAL_API_ERROR, 
                "Failed to process employee data from API", 
                e
            );
        }
    }

    /**
     * Safely extract a String value from a map using Optional
     * 
     * @param map source map
     * @param key key to extract
     * @return string value or empty string
     */
    private String getStringValue(Map<String, Object> map, String key) {
        return Optional.ofNullable(map.get(key))
                .map(Object::toString)
                .orElse("");
    }

    /**
     * Safely extract an Integer value from a map using Optional
     * 
     * @param map source map
     * @param key key to extract
     * @return integer value or 0
     */
    private Integer getIntegerValue(Map<String, Object> map, String key) {
        return Optional.ofNullable(map.get(key))
                .map(Object::toString)
                .map(s -> {
                    try {
                        return Integer.parseInt(s);
                    } catch (NumberFormatException e) {
                        logger.warn("Failed to parse integer value for key {}: {}", key, s);
                        return 0;
                    }
                })
                .orElse(0);
    }
    
    /**
     * Response wrapper for the mock API
     */
    private static class MockApiResponse<T> {
        private T data;
        private String status;
        
        public T getData() {
            return data;
        }
        
        public void setData(T data) {
            this.data = data;
        }
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
    }
    
    /**
     * Generic fallback method for retry failures
     * Called when all retry attempts fail for any API method
     * 
     * @param e the exception that caused retries to fail
     * @return never returns, throws exception
     * @throws ExternalApiException with appropriate error message
     */
    @Recover
    public <T> T recoverApiCall(Exception e) {
        logger.error("API call failed after multiple retry attempts", e);
        
        // No need to handle RateLimitedException here since it's already handled in the main methods
        
        throw new ExternalApiException(
            ApiErrorCode.EXTERNAL_API_UNAVAILABLE,
            "API request failed after multiple attempts. Please try again later.",
            e
        );
    }
}
