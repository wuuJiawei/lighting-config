package io.lighting.config.server.rest.dto;

import static io.lighting.config.server.web.ApiConstants.BEARER_TOKEN_TYPE;

public class LoginResponse {

    private static final long DEFAULT_TOKEN_TTL_SECONDS = 86_400L;
    private static final String DISABLED_TOKEN = "console-bypass";

    private final String accessToken;
    private final String tokenType;
    private final long expiresIn;

    private LoginResponse(String accessToken, String tokenType, long expiresIn) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
    }

    public static LoginResponse success(String token) {
        return new LoginResponse(token, BEARER_TOKEN_TYPE, DEFAULT_TOKEN_TTL_SECONDS);
    }

    public static LoginResponse disabled() {
        return new LoginResponse(DISABLED_TOKEN, BEARER_TOKEN_TYPE, DEFAULT_TOKEN_TTL_SECONDS);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }
}
