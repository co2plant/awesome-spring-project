package com.ssafy.edu.awesomeproject.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class CommonBeanConfig {

    @Bean
    public Clock applicationClock() {
        return Clock.systemUTC();
    }
}
