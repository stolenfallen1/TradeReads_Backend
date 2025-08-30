package com.tradereads.components;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.tradereads.components.JwtAuthenticationFilter.CustomAuthenticationDetails;

@Component
public class AuthUtil {
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof CustomAuthenticationDetails) {
            CustomAuthenticationDetails details = (CustomAuthenticationDetails) authentication.getDetails();
            return details.getUserId();
        }
        throw new RuntimeException("No authenticated user found");
    }

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getName();
        }
        throw new RuntimeException("No authenticated user found");
    }

    public String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof CustomAuthenticationDetails) {
            CustomAuthenticationDetails details = (CustomAuthenticationDetails) authentication.getDetails();
            return details.getRole();
        }
        throw new RuntimeException("No authenticated user found");
    }

    public boolean isCurrentUser(Long userId) {
        try {
            return getCurrentUserId().equals(userId);
        } catch (RuntimeException e) {
            return false;
        }
    }
}
