package com.ssafy.edu.awesomeproject.common.error;

import org.springframework.http.HttpStatus;

public enum CommonErrorCode implements ErrorCode {
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_400_1", "Invalid input."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "COMMON_400_2", "Validation failed."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_404_1", "Resource not found."),
    BUSINESS_RULE_VIOLATION(HttpStatus.CONFLICT, "COMMON_409_1", "Business rule violation."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500_1", "Unexpected server error.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    CommonErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatus status() {
        return status;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
