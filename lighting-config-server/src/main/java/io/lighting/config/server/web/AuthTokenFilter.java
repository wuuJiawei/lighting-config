package io.lighting.config.server.web;

import io.lighting.config.server.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
        if (!path.startsWith("/lighting-config/api") || path.startsWith("/lighting-config/api/auth")) {
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
        response.getWriter().write("Unauthorized");
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        String fallback = request.getHeader("X-Lighting-Token");
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        return null;
    }
}
