package com.tradereads.controller;

import com.tradereads.dto.JwtResponseDTO;
import com.tradereads.dto.LoginRequestDTO;
import com.tradereads.dto.LogoutRequestDTO;
import com.tradereads.dto.RefreshTokenRequestDTO;
import com.tradereads.model.RefreshToken;
import com.tradereads.model.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradereads.service.JwtService;
import com.tradereads.service.RefreshTokenService;
import com.tradereads.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Value("${jwt.access-token-expiration:600000}") // 10 minutes in milliseconds
    private long accessTokenExpiration;

    public AuthController(UserService userService, JwtService jwtService, RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user, HttpServletRequest request) {
        try {
            User registeredUser = userService.registerUser(
                user.getUsername(), 
                user.getPassword(), 
                user.getUserRole(), user.getEmail(), 
                user.getPhoneNumber()
            );

            registeredUser.setPassword(null); // Never return password in response
            return ResponseEntity.ok(Map.of("message", "User registered successfully"));
        } catch(IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch(Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequest, HttpServletRequest request) {
        try {
            Optional<User> authenticateUser = userService.authenticateUser(
                loginRequest.getUsername(), 
                loginRequest.getPassword()
            );

            if (authenticateUser.isPresent()) {
                User user = authenticateUser.get();

                String accessToken = jwtService.generateToken(
                    user.getUsername(),
                    user.getId(),
                    user.getUserRole()
                );

                String refreshToken = refreshTokenService.createRefreshToken(
                    user.getId(), 
                    extractDeviceInfo(request), 
                    getClientIpAddress(request)
                );

                user.setPassword(null); // Avoid sending password back in response
                return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "user", user,
                    "tokens", new JwtResponseDTO(accessToken, refreshToken, accessTokenExpiration)
                ));
            }

            return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
        } catch(Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO request) {
        try {
            String refreshToken = request.getRefreshToken();

            if (!refreshTokenService.validateRefreshToken(refreshToken)) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired refresh token"));
            }

            Optional<Long> userId = refreshTokenService.getUserIdFromRefreshToken(refreshToken);
            if (userId.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid refresh token"));
            }

            Optional<User> user = userService.getUserById(userId.get());
            if (user.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of("error", "User not found"));
            }

            String newAccessToken = jwtService.generateAccessTokenFromRefreshToken(
                user.get().getUsername(),
                user.get().getId(),
                user.get().getUserRole()
            );

            return ResponseEntity.ok(Map.of(
                "accessToken", newAccessToken,
                "tokenType", "Bearer",
                "expiresIn", accessTokenExpiration
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutRequestDTO request, HttpServletRequest httpRequest) {
        try {
            String refreshToken = request.getRefreshToken();
            if (refreshToken == null || refreshToken.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Refresh token required"));
            }

            String accessToken = extractTokenFromRequest(httpRequest);
            if (accessToken == null || accessToken.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Unauthorized"));
            }

            Long userId = jwtService.extractUserId(accessToken);
            Optional<Long> refreshTokenUserId = refreshTokenService.getUserIdFromRefreshToken(refreshToken);
            if (refreshTokenUserId.isEmpty() || !refreshTokenUserId.get().equals(userId)) {
                return ResponseEntity.status(403).body(Map.of("error", "Invalid token for this user"));
            }

            refreshTokenService.revokeRefreshToken(refreshToken);
            return ResponseEntity.ok(Map.of("message", "Logout successful"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/logout-all")
    public ResponseEntity<?> logoutAll(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token != null) {
                Long userId = jwtService.extractUserId(token);
                refreshTokenService.revokeAllUserTokens(userId);
            }

            return ResponseEntity.ok(Map.of("message", "Logged out from all device"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/sessions")
    public ResponseEntity<?> getActiveSessions(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token != null) {
                Long userId = jwtService.extractUserId(token);
                List<RefreshToken> sessions = refreshTokenService.getUserActiveSessions(userId);
                return ResponseEntity.ok(sessions);
            }

            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    // Helper methods 
    // NOTE: Subject to change as public method and move in the future for reusability.
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private String extractDeviceInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent : "Unreported Device";
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
