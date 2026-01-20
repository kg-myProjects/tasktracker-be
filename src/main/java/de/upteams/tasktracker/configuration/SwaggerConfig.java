package de.upteams.tasktracker.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SwaggerConfig {
    public static final String COOKIE_AUTH = "cookieAuth";


    @Bean
    public OpenAPI taskTrackerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Task Tracker API")
                        .version("0.0.1")
                        .description("API with JWT in HTTP-only cookies"))
                .addSecurityItem(
                        new SecurityRequirement().addList(COOKIE_AUTH)
                )
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        COOKIE_AUTH,
                                        new SecurityScheme()
                                                .name("ACCESS_TOKEN") // имя cookie
                                                .type(SecurityScheme.Type.APIKEY)
                                                .in(SecurityScheme.In.COOKIE)
                                )
                );

    }
}
