package io.lighting.config.server.service;

import io.lighting.config.server.config.LightingServerProperties;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final LightingServerProperties properties;

    public AuthService(LightingServerProperties properties) {
        this.properties = properties;
    }

    public boolean isEnabled() {
        return properties.getAuth().isEnabled();
    }

    public boolean authenticate(String token) {
        if (!isEnabled()) {
            return true;
        }
        if (token == null || token.isBlank()) {
            return false;
        }
        return resolvedTokens().contains(token.trim());
    }

    public Set<String> resolvedTokens() {
        Map<String, String> options = properties.getAuth().getOptions();
        String tokens = options.getOrDefault("tokens", options.getOrDefault("token", ""));
        if (tokens == null || tokens.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(tokens.split(","))
                .map(String::trim)
                .filter(entry -> !entry.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }
}
