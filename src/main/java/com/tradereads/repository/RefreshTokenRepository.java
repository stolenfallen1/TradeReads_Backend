package com.tradereads.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tradereads.model.RefreshToken;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findByUserId(Long userId);
    void deleteByToken(String token);
    void deleteByUserId(Long userId);

    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    @Modifying
    void deleteExpiredTokens(@Param("now") Instant now);

    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.userId = :userId AND rt.expiresAt > :now")
    long countActiveTokensForUser(@Param("userId") Long userId, @Param("now") Instant now);
}
