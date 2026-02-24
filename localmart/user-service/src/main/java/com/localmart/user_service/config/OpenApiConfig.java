package com.localmart.user_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Provides the title/description/version metadata shown at the top of Swagger UI.
// SpringDoc picks this bean up automatically and injects it into the generated OpenAPI spec.
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI userServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LocalMart - User Service API")
                        .description("Manages customer and shop owner profiles for the LocalMart marketplace")
                        .version("1.0.0"));
    }
}
