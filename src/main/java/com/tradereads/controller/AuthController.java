package com.tradereads.controller;

import com.tradereads.dto.LoginRequestDTO;
import com.tradereads.model.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradereads.service.JwtService;
import com.tradereads.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user) {
        try {
            User registeredUser = userService.registerUser(
                user.getUsername(), 
                user.getPassword(), 
                user.getRole(), user.getEmail(), 
                user.getPhoneNumber()
            );

            String token = jwtService.generateToken(
                registeredUser.getUsername(),
                registeredUser.getId(),
                registeredUser.getRole()
            );

            registeredUser.setPassword(null); // Never return password in response
            return ResponseEntity.ok(Map.of(
                "message", "User registered successfully",
                "user", registeredUser,
                "token", token
            ));
        } catch(IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            Optional<User> authenticateUser = userService.authenticateUser(
                loginRequest.getUsername(), 
                loginRequest.getPassword()
            );

            if (authenticateUser.isPresent()) {
                User user = authenticateUser.get();
                String token = jwtService.generateToken(
                    user.getUsername(),
                    user.getId(),
                    user.getRole()
                );
                user.setPassword(null); // Avoid sending password back in response
                return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "user", user,
                    "token", token
                ));
            }

            return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
        } catch(Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }
}
