package io.lighting.config.core.api;

import java.util.Objects;

/**
 * Result of an authentication operation.
 */
public final class AuthResult {

    public enum Status {
        SUCCESS,
        UNAUTHORIZED,
        FORBIDDEN
    }

    private final Status status;
    private final String principal;
    private final String message;

    private AuthResult(Status status, String principal, String message) {
        this.status = Objects.requireNonNull(status, "status");
        this.principal = principal;
        this.message = message;
    }

    public static AuthResult success(String principal) {
        return new AuthResult(Status.SUCCESS, principal, "");
    }

    public static AuthResult unauthorized(String message) {
        return new AuthResult(Status.UNAUTHORIZED, null, message);
    }

    public static AuthResult forbidden(String message) {
        return new AuthResult(Status.FORBIDDEN, null, message);
    }

    public Status getStatus() {
        return status;
    }

    public String getPrincipal() {
        return principal;
    }

    public String getMessage() {
        return message;
    }
}
