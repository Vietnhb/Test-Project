package com.fpt.hivtreatment.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

import com.fpt.hivtreatment.security.jwt.AuthEntryPointJwt;
import com.fpt.hivtreatment.security.jwt.AuthTokenFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class WebSecurityConfig {

    private final AuthEntryPointJwt unauthorizedHandler;
    private final CorsFilter corsFilter;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Configuring Spring Security...");

        http.csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    // Public endpoints
                    auth.requestMatchers("/api/auth/**").permitAll();
                    auth.requestMatchers("/api/public/**").permitAll();
                    auth.requestMatchers("/api/doctors/**").permitAll();
                    auth.requestMatchers("/api/test-data/**").permitAll();

                    // Admin and Manager only endpoints
                    auth.requestMatchers("/api/admin/**").hasAnyAuthority("4", "5");

                    // Role-specific endpoints
                    auth.requestMatchers("/api/doctor/**").hasAuthority("2");
                    auth.requestMatchers("/api/staff/**").hasAuthority("3");
                    auth.requestMatchers("/api/patient/**").hasAuthority("1");

                    // Admin-only endpoints
                    auth.requestMatchers("/api/users/**").hasAuthority("4");

                    // Manager-only endpoints
                    auth.requestMatchers("/api/manager/**").hasAuthority("5");

                    // Authenticated endpoints (all users)
                    auth.requestMatchers("/api/profile/**").authenticated();

                    // Default - require authentication
                    auth.anyRequest().authenticated();

                    log.info("Security paths configured with role-based access control");
                });

        http.addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        log.info("Spring Security configuration completed");
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}