package io.lighting.config.server.web;

import io.lighting.config.server.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import static io.lighting.config.server.web.ApiConstants.API_BASE_PATH;
import static io.lighting.config.server.web.ApiConstants.AUTH_PATH;
import static io.lighting.config.server.web.ApiConstants.DOCS_PATH;
import static io.lighting.config.server.web.ApiConstants.HEADER_AUTHORIZATION_BEARER_PREFIX;
import static io.lighting.config.server.web.ApiConstants.HEADER_TOKEN;
import static io.lighting.config.server.web.ApiConstants.OPENAPI_PATH;
import static io.lighting.config.server.web.ApiConstants.SWAGGER_UI_SEGMENT;
import static io.lighting.config.server.web.ApiConstants.UNAUTHORIZED_MESSAGE;

public class AuthTokenFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AuthTokenFilter.class);
    private final AuthService authService;

    public AuthTokenFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!authService.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }
        String path = request.getRequestURI();
        if (!path.startsWith(API_BASE_PATH)
                || path.startsWith(AUTH_PATH)
                || path.startsWith(OPENAPI_PATH)
                || path.startsWith(DOCS_PATH)
                || path.contains(SWAGGER_UI_SEGMENT)
        ) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = resolveToken(request);
        if (authService.authenticate(token)) {
            filterChain.doFilter(request, response);
            return;
        }
        log.warn("Rejecting {} {} due to missing/invalid auth token", request.getMethod(), path);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write(UNAUTHORIZED_MESSAGE);
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(HEADER_AUTHORIZATION_BEARER_PREFIX)) {
            return header.substring(HEADER_AUTHORIZATION_BEARER_PREFIX.length());
        }
        String fallback = request.getHeader(HEADER_TOKEN);
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        return null;
    }
}
