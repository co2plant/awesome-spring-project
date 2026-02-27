package com.ssafy.edu.awesomeproject.sample.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EchoRequest(
        @NotBlank(message = "message must not be blank.")
        @Size(max = 200, message = "message must be less than or equal to 200 characters.")
        String message
) {
}
