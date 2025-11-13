package io.lighting.config.server.rest.dto;

import javax.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
