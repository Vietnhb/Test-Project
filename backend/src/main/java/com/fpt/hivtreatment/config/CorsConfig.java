package com.fpt.hivtreatment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Value("${app.cors.allowedOrigins}")
    private String allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Cho phép sử dụng credentials (như cookies, authorization headers)
        config.setAllowCredentials(true);

        // Thêm các origins được phép từ application.properties
        for (String origin : allowedOrigins.split(",")) {
            config.addAllowedOrigin(origin.trim());
        }

        // Cho phép tất cả các headers và methods
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        // Thời gian cache preflight requests (OPTIONS)
        config.setMaxAge(3600L);

        // Áp dụng cấu hình cho tất cả các endpoints
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}