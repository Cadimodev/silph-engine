package com.silphengine.domain.services;

import com.silphengine.domain.entities.RefreshToken;
import com.silphengine.domain.entities.User;

import java.util.Optional;

public interface RefreshTokenService {
    Optional<RefreshToken> findByToken(String token);
    RefreshToken createRefreshToken(User user);
    RefreshToken verifyExpiration(RefreshToken token);
    void deleteByToken(String token);
}