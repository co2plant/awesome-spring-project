package com.ssafy.edu.awesomeproject.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ssafy.edu.awesomeproject.common.error.ErrorCode;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data,
        OffsetDateTime timestamp
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "SUCCESS", "Request processed successfully.", data, OffsetDateTime.now(ZoneOffset.UTC));
    }

    public static ApiResponse<Void> fail(ErrorCode errorCode) {
        return new ApiResponse<>(false, errorCode.code(), errorCode.message(), null, OffsetDateTime.now(ZoneOffset.UTC));
    }

    public static <T> ApiResponse<T> fail(ErrorCode errorCode, T data) {
        return new ApiResponse<>(false, errorCode.code(), errorCode.message(), data, OffsetDateTime.now(ZoneOffset.UTC));
    }
}
