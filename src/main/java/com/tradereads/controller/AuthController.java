package com.tradereads.controller;

import com.tradereads.dto.LoginRequest;
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

import com.tradereads.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
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
            registeredUser.setPassword(null); // Never return password in response
            return ResponseEntity.ok(registeredUser);
        } catch(IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Optional<User> authenticateUser = userService.authenticateUser(
                loginRequest.getUsername(), 
                loginRequest.getPassword()
            );

            if (authenticateUser.isPresent()) {
                User user = authenticateUser.get();
                user.setPassword(null); // Avoid sending password back in response
                return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "user", user
                ));
            }

            return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
        } catch(Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }
}
