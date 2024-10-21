package com.greentrade.greentrade.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI greenTradeOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("GreenTrade API")
                        .description("API voor de GreenTrade applicatie")
                        .version("v0.0.1"));
    }
}