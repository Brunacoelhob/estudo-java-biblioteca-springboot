package com.biblioteca.digital.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    /** Origens permitidas vêm da configuração (app.cors.origens), não do código. */
    @Bean
    public WebMvcConfigurer corsConfigurer(@Value("${app.cors.origens}") String[] origens) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/livros/**")
                        .allowedOrigins(origens)
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .allowedHeaders("Content-Type")
                        .maxAge(3600);
            }
        };
    }
}
