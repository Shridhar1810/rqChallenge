package com.reliaquest.api.exception;

import lombok.Getter;

/**
 * Standardized error codes for API responses
 */
@Getter
public enum ApiErrorCode {
    // General errors
    UNKNOWN_ERROR("ERR-GEN-001", "An unknown error occurred"),
    VALIDATION_ERROR("ERR-GEN-002", "Validation error"),
    
    // External API errors
    EXTERNAL_API_UNAVAILABLE("ERR-EXT-001", "External API is unavailable"),
    EXTERNAL_API_RATE_LIMITED("ERR-EXT-003", "External API rate limit exceeded"),
    EXTERNAL_API_ERROR("ERR-EXT-004", "External API returned an error"),
    EXTERNAL_API_TIMEOUT("ERR-EXT-005", "External API request timed out"),

    // Resource errors
    RESOURCE_NOT_FOUND("ERR-RES-001", "Resource not found");

    private final String code;
    private final String defaultMessage;
    
    ApiErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

}
