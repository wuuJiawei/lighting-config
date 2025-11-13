package io.lighting.config.server.rest.dto;

public class LoginResponse {

    private final String accessToken;
    private final String tokenType;
    private final long expiresIn;

    private LoginResponse(String accessToken, String tokenType, long expiresIn) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
    }

    public static LoginResponse success(String token) {
        return new LoginResponse(token, "bearer", 86_400L);
    }

    public static LoginResponse disabled() {
        return new LoginResponse("console-bypass", "bearer", 86_400L);
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
