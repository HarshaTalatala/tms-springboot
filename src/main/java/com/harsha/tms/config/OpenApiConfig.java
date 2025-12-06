package com.harsha.tms.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Transport Management System API")
                        .version("1.0.0")
                        .description("Comprehensive API documentation for the Transport Management System backend. This system provides endpoints for managing transporters, loads, bids, and bookings in an efficient and scalable manner.")
                        .contact(new Contact()
                                .name("Harsha Vardhan Reddy Talatala")
                                .email("harsha.talatala@gmail.com")));
    }
}
