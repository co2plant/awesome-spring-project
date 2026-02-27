package com.ssafy.edu.awesomeproject.sample.presentation;

import com.ssafy.edu.awesomeproject.common.error.CommonErrorCode;
import com.ssafy.edu.awesomeproject.common.error.CommonException;
import com.ssafy.edu.awesomeproject.common.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/v1/examples")
public class ExampleController {

    private final Clock clock;

    public ExampleController(Clock clock) {
        this.clock = clock;
    }

    @GetMapping("/ping")
    public ApiResponse<Map<String, Object>> ping() {
        return ApiResponse.success(Map.of(
                "message", "pong",
                "serverTime", OffsetDateTime.now(clock)
        ));
    }

    @PostMapping("/echo")
    public ApiResponse<Map<String, String>> echo(@Valid @RequestBody EchoRequest request) {
        return ApiResponse.success(Map.of(
                "message", request.message().trim(),
                "length", String.valueOf(request.message().trim().length())
        ));
    }

    @GetMapping("/rules/{number}")
    public ApiResponse<Map<String, Integer>> rules(@PathVariable @Positive int number) {
        if (number % 2 != 0) {
            throw new CommonException(CommonErrorCode.BUSINESS_RULE_VIOLATION, Map.of("number", number));
        }
        return ApiResponse.success(Map.of("acceptedNumber", number));
    }
}
