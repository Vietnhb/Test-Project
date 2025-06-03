package com.fpt.hivtreatment.security.jwt;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fpt.hivtreatment.security.services.UserDetailsServiceImpl;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@NoArgsConstructor
public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String requestURI = request.getRequestURI();

            // Don't log health checks and other common endpoints
            if (!requestURI.contains("/actuator") && !requestURI.contains("/favicon.ico")) {
                log.info("Processing request: {} {}", request.getMethod(), requestURI);
            }

            String jwt = parseJwt(request);
            if (jwt != null) {
                if (!requestURI.contains("/actuator")) {
                    log.info("JWT found in request for URI: {}", requestURI);
                }

                if (jwtUtils.validateJwtToken(jwt)) {
                    String username = jwtUtils.getUserNameFromJwtToken(jwt);
                    log.info("JWT valid for user: {}, URI: {}", username, requestURI);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (userDetails != null) {
                        log.info("User details loaded. Username: {}, Authorities: {}",
                                userDetails.getUsername(), userDetails.getAuthorities());

                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.info("Authentication set in SecurityContext for '{}', URI: {}", username, requestURI);
                    } else {
                        log.warn("Could not load user details for username: {}", username);
                    }
                } else {
                    log.warn("Invalid JWT token for URI: {}", requestURI);
                }
            } else {
                if (!requestURI.contains("/actuator") && !requestURI.equals("/api/auth/login") &&
                        !requestURI.contains("/public") && !requestURI.contains("/assets")) {
                    log.info("No JWT token found in request for URI: {}", requestURI);
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            String token = headerAuth.substring(7);
            log.debug("Bearer token extracted from request, length: {}", token.length());
            return token;
        }

        return null;
    }
}