package com.tradereads.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tradereads.model.User;
import com.tradereads.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User registerUser(String username, String password, String role, String email, String phoneNumber) {
        if (password == null || password.length() < 8 || password.length() > 16) {
            throw new IllegalArgumentException("Password must not be less than 8 characters and more than 16 characters");
        }

        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already registered to another account");
        }
        if (userRepository.findByPhoneNumber(phoneNumber).isPresent()) {
            throw new IllegalArgumentException("Phone number already connected to another account");
        }

        // Provide default role if not provided
        if (role == null || role.trim().isEmpty()) {
            role = "USER";
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        return userRepository.save(user);
    }

    public Optional<User> authenticateUser(String username, String password) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return userOptional;
            }
        }
        return Optional.empty();
    }
}
