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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final AuthEntryPointJwt unauthorizedHandler;
    private final CorsFilter corsFilter;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    // WebSocket endpoints
                    auth.requestMatchers("/ws/**").permitAll();
                    auth.requestMatchers("/api/ws/**").permitAll();

                    // Debug endpoints - no authentication required
                    auth.requestMatchers("/debug/**").permitAll();
                    auth.requestMatchers("/api/debug/**").permitAll();
                    auth.requestMatchers("/debug/database-tables").permitAll();
                    auth.requestMatchers("/api/debug/database-tables").permitAll();

                    // Swagger UI endpoints
                    auth.requestMatchers("/swagger-ui.html").permitAll();
                    auth.requestMatchers("/swagger-ui/**").permitAll();
                    auth.requestMatchers("/api-docs/**").permitAll();
                    auth.requestMatchers("/v3/api-docs/**").permitAll();

                    // Public endpoints
                    auth.requestMatchers("/api/auth/**").permitAll();
                    auth.requestMatchers("/api/public/**").permitAll();
                    auth.requestMatchers("/api/doctors/**").permitAll();
                    auth.requestMatchers("/api/test-data/**").permitAll();
                    auth.requestMatchers("/api/test-types/**").permitAll(); // Endpoint xét nghiệm không yêu cầu xác
                                                                            // thực
                    auth.requestMatchers("/appointments/test").permitAll(); // Lab test orders endpoints - accessible by
                                                                            // authenticated users
                    auth.requestMatchers("/api/lab-test-orders/**").hasAnyAuthority("1", "2", "3", "5"); // Patient,
                                                                                                         // Doctor,
                                                                                                         // Staff,
                                                                                                         // Manager

                    // Treatment protocols endpoints - accessible by doctors
                    auth.requestMatchers("/api/treatment-protocols/**").permitAll();
                    // Payment endpoints
                    auth.requestMatchers("/api/payments/**").hasAnyAuthority("2", "3");// Patient, Doctor,
                                                                                       // Staff, Manager

                    // Chat endpoints
                    auth.requestMatchers("/api/chat/**").authenticated();
                    auth.requestMatchers("/topic/**").permitAll();
                    auth.requestMatchers("/app/**").permitAll();

                    // Thêm các đường dẫn /api/... tương ứng
                    auth.requestMatchers("/api/appointments").authenticated();
                    auth.requestMatchers("/api/appointments/**").authenticated();
                    auth.requestMatchers("/api/patient/appointments").authenticated();
                    auth.requestMatchers("/api/doctor/appointments").authenticated();

                    // Admin and Manager only endpoints
                    auth.requestMatchers("/api/admin/**").hasAnyAuthority("4", "5");

                    // Role-specific endpoints
                    auth.requestMatchers("/api/doctor/**").hasAuthority("2");
                    auth.requestMatchers("/api/staff/**").hasAuthority("3");
                    auth.requestMatchers("/api/patient/**").hasAuthority("1");

                    // Admin-only endpoints
                    auth.requestMatchers("/api/users/**").hasAnyAuthority("4", "2", "3");

                    // Manager-only endpoints
                    auth.requestMatchers("/api/manager/**").hasAuthority("5");
                    auth.requestMatchers("/manager/**").hasAuthority("5");

                    // Authenticated endpoints (all users)
                    auth.requestMatchers("/api/profile/**").authenticated();

                    // Default - require authentication
                    auth.anyRequest().authenticated();
                });

        http.addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

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