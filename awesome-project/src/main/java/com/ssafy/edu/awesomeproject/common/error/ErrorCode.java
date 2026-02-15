package com.ssafy.edu.awesomeproject.common.error;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    HttpStatus status();
    String code();
    String message();
}
