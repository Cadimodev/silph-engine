package com.silphengine.domain.services;

import com.silphengine.domain.dto.requests.LoginRequest;
import com.silphengine.domain.dto.requests.RefreshTokenRequest;
import com.silphengine.domain.dto.requests.UserRequest;
import com.silphengine.domain.dto.responses.AuthResponse;

public interface AuthenticationService {

    AuthResponse register(UserRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);
}
