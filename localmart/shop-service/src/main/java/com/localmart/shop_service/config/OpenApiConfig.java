package com.localmart.shop_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Configuration — tells Spring to load this class at startup and run its @Bean methods.
 * Spring registers the returned object in the application context (its internal object registry).
 *
 * This class provides the "header metadata" for the OpenAPI spec that SpringDoc generates.
 * Without this bean, SpringDoc still generates docs but shows generic defaults
 * (title = "OpenAPI definition", no description, version = "1.0").
 *
 * How the pieces connect:
 *   springdoc dependency → provides the scanner and Swagger UI server
 *   This @Bean           → fills the title/description/version shown at the top of Swagger UI
 *   @Tag, @Operation     → describe each endpoint (in ShopController)
 *   @Schema              → describe each field (in DTOs)
 */
@Configuration
public class OpenApiConfig {

    /**
     * @Bean — Spring calls this method once at startup and stores the returned OpenAPI object.
     * SpringDoc finds this bean in the context and uses it to populate the spec info section.
     *
     * Package-private (no 'public') — Spring 6+ does not require public @Bean methods.
     * Only Spring calls this internally — no external code needs access to it.
     */
    @Bean
    OpenAPI shopServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LocalMart - Shop Service API")
                        .description("Manages shops and their products for the LocalMart marketplace")
                        .version("1.0.0"));
    }
}
