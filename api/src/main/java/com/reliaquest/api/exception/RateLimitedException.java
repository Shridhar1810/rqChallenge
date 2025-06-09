package com.reliaquest.api.exception;

import lombok.Getter;

/**
 * Exception thrown when a rate limit is exceeded (429 Too Many Requests)
 */
@Getter
public class RateLimitedException extends RuntimeException {

    /**
     * The recommended wait time in seconds before retrying
     */
    private final int retryAfterSeconds;
    
    public RateLimitedException(String message, int retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }
    
    public RateLimitedException(String message, Throwable cause, int retryAfterSeconds) {
        super(message, cause);
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
