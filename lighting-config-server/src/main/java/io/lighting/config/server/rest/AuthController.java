package io.lighting.config.server.rest;

import io.lighting.config.server.rest.dto.LoginRequest;
import io.lighting.config.server.rest.dto.LoginResponse;
import io.lighting.config.server.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;

@RestController
@RequestMapping("/lighting-config/api/auth")
@Validated
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        if (!authService.isEnabled()) {
            return ResponseEntity.ok(LoginResponse.disabled());
        }
        if (!authService.authenticate(request.getToken())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        return ResponseEntity.ok(LoginResponse.success(request.getToken()));
    }
}
