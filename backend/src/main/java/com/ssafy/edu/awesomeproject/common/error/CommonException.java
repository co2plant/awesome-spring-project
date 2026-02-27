package com.ssafy.edu.awesomeproject.common.error;

public class CommonException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Object details;

    public CommonException(ErrorCode errorCode) {
        this(errorCode, null);
    }

    public CommonException(ErrorCode errorCode, Object details) {
        super(errorCode.message());
        this.errorCode = errorCode;
        this.details = details;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Object getDetails() {
        return details;
    }
}
