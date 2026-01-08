package ru.practicum.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.controller.ErrorHandler;

@Configuration
public class GlobalErrorHandlerConfig {
    @Bean
    public ErrorHandler globalErrorHandler() {
        return new ErrorHandler();
    }
}
