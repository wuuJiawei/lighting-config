package io.lighting.config.server.lock;

import jakarta.servlet.http.HttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static io.lighting.config.server.web.ApiConstants.HEADER_EDITOR_ID;
import static io.lighting.config.server.web.ApiConstants.HEADER_EDITOR_NAME;
import static io.lighting.config.server.web.ApiConstants.REQUEST_ATTRIBUTE_AUTH_TOKEN;

public class LockOwnerResolver {

    public LockOwner resolve(HttpServletRequest request) {
        return resolve(request, null);
    }

    public LockOwner resolve(HttpServletRequest request, String preferredName) {
        String sessionId = firstNonBlank(
                request.getHeader(HEADER_EDITOR_ID),
                request.getParameter("sessionId"),
                "console-session");
        String token = (String) request.getAttribute(REQUEST_ATTRIBUTE_AUTH_TOKEN);
        String ownerSource = token == null ? sessionId : token + ":" + sessionId;
        String ownerId = sha256(ownerSource);
        String ownerName = firstNonBlank(preferredName, request.getHeader(HEADER_EDITOR_NAME), request.getParameter("editorName"), "控制台用户");
        return new LockOwner(ownerId, ownerName);
    }

    private String firstNonBlank(String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate.trim();
            }
        }
        return "";
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hashed.length * 2);
            for (byte b : hashed) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm missing", e);
        }
    }
}
