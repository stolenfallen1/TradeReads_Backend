package com.tradereads.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradereads.model.RefreshToken;
import com.tradereads.repository.RefreshTokenRepository;

@Service
@Transactional
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiration:604800000}") // 7 days in milliseconds
    private long refreshTokenExpiration;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String createRefreshToken(Long userId, String deviceInfo, String ipAddress) {
        // Limit concurrent sessions per user 
        long activeTokens = refreshTokenRepository.countActiveTokensForUser(userId, Instant.now());
        if (activeTokens >= 5) {
            List<RefreshToken> userTokens = refreshTokenRepository.findByUserId(userId);
            userTokens.stream()
                .filter(token -> token.getExpiresAt().isAfter(Instant.now()))
                .min((t1, t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt()))
                .ifPresent(oldestToken -> refreshTokenRepository.delete(oldestToken));
        }

        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plusMillis(refreshTokenExpiration);

        RefreshToken refreshToken = new RefreshToken(token, userId, expiresAt, Instant.now(), deviceInfo, ipAddress);
        refreshTokenRepository.save(refreshToken);

        return token;
    }

    public boolean validateRefreshToken(String token) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(token);
        return refreshToken.isPresent() && refreshToken.get().getExpiresAt().isAfter(Instant.now());
    }

    public Optional<Long> getUserIdFromRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token)
            .filter(rt -> rt.getExpiresAt().isAfter(Instant.now()))
            .map(RefreshToken::getUserId);
    }

    public void revokeRefreshToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }

    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    public List<RefreshToken> getUserActiveSessions(Long userId) {
        return refreshTokenRepository.findByUserId(userId)
            .stream()
            .filter(token -> token.getExpiresAt().isAfter(Instant.now()))
            .toList();
    }

    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(Instant.now());
    }
}
