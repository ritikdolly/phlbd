package com.phlbd.controller;

import com.phlbd.common.ApiResponse;
import com.phlbd.dto.auth.*;
import com.phlbd.security.SecurityUtils;
import com.phlbd.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for registration, login, JWT token refresh, and logout")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new customer account")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and issue JWT access & refresh tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Logged in successfully", response));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Generate a new access token using a valid refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke user refresh tokens and log out")
    public ResponseEntity<ApiResponse<Void>> logout() {
        String userId = SecurityUtils.getCurrentUserId();
        authService.logout(userId);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser() {
        String userId = SecurityUtils.getCurrentUserId();
        UserDto userDto = authService.getCurrentUser(userId);
        return ResponseEntity.ok(ApiResponse.success(userDto));
    }
}
