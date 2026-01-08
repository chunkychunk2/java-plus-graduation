package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import ru.practicum.configuration.GlobalErrorHandlerConfig;
import ru.practicum.configuration.JacksonConfig;

@EnableFeignClients
@EnableDiscoveryClient
@Import({GlobalErrorHandlerConfig.class, JacksonConfig.class})
@SpringBootApplication
public class EventServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(EventServiceApp.class, args);
    }
}
