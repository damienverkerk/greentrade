package com.greentrade.greentrade.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI greenTradeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GreenTrade API")
                        .description("API voor de GreenTrade applicatie - Een platform voor handel in duurzame producten")
                        .version("v0.0.1")
                        .contact(new Contact()
                                .name("GreenTrade Support")
                                .email("support@greentrade.nl")
                                .url("https://www.greentrade.nl"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development server")
                ));
    }
}