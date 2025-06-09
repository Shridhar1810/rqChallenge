package com.reliaquest.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when an external API call fails
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ExternalApiException extends RuntimeException {

    private final ApiErrorCode errorCode;
    private final HttpStatus httpStatus;

    public ExternalApiException(ApiErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = mapToHttpStatus(errorCode);
    }

    public ExternalApiException(ApiErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = mapToHttpStatus(errorCode);
    }

    public ApiErrorCode getErrorCode() {
        return errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    private HttpStatus mapToHttpStatus(ApiErrorCode errorCode) {
        switch (errorCode) {
            case EXTERNAL_API_RATE_LIMITED:
                return HttpStatus.TOO_MANY_REQUESTS;
            case RESOURCE_NOT_FOUND:
                return HttpStatus.NOT_FOUND;
            case EXTERNAL_API_UNAVAILABLE:
            case EXTERNAL_API_TIMEOUT:
                return HttpStatus.SERVICE_UNAVAILABLE;
            default:
                return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
