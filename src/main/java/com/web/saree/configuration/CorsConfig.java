package com.web.saree.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**") // Apply to all your API endpoints
                        .allowedOrigins("https://saree-frontend.vercel.app", "http://localhost:3000") // Add your Vercel URL and localhost
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allow all standard methods
                        .allowedHeaders("*") // Allow all headers
                        .allowCredentials(true); // Allow credentials (like cookies)
            }
        };
    }
}