package com.ssafy.edu.awesomeproject.sample.presentation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ExampleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void ping_returnsSuccessEnvelope() throws Exception {
        mockMvc.perform(get("/api/v1/examples/ping"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Request-Id"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.message").value("pong"));
    }

    @Test
    void echo_whenBlankMessage_returnsValidationFailureEnvelope() throws Exception {
        mockMvc.perform(post("/api/v1/examples/echo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COMMON_400_2"));
    }

    @Test
    void rules_whenOddNumber_returnsBusinessErrorEnvelope() throws Exception {
        mockMvc.perform(get("/api/v1/examples/rules/3"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COMMON_409_1"))
                .andExpect(jsonPath("$.data.number").value(3));
    }
}
