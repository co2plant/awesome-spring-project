package com.ssafy.edu.awesomeproject.common.error;

public record ValidationError(
        String field,
        String reason
) {
}
