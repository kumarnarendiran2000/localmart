package com.localmart.order_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderServiceOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Order Service API")
                .description("Handles order placement with sync validation via shop-service and user-service")
                .version("1.0.0"));
    }
}
