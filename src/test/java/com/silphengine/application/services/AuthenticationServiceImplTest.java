package com.silphengine.application.services;

import com.silphengine.domain.dto.requests.LoginRequest;
import com.silphengine.domain.dto.requests.RefreshTokenRequest;
import com.silphengine.domain.dto.requests.UserRequest;
import com.silphengine.domain.dto.responses.AuthResponse;
import com.silphengine.domain.entities.RefreshToken;
import com.silphengine.domain.entities.User;
import com.silphengine.domain.enums.Role;
import com.silphengine.domain.exceptions.TokenRefreshException;
import com.silphengine.domain.services.RefreshTokenService;
import com.silphengine.domain.services.UserService;
import com.silphengine.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private User user;
    private UserRequest userRequest;
    private LoginRequest loginRequest;
    private RefreshToken refreshToken;
    private final String accessToken = "dummy-access-token";
    private final String refreshTokenString = "dummy-refresh-token";

    @BeforeEach
    void setUp() {

        userRequest = new UserRequest("testuser", "test@example.com", "password123");
        loginRequest = new LoginRequest("testuser", "password123");

        user = User.builder()
                .id(UUID.randomUUID())
                .nickname("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .role(Role.USER)
                .build();

        refreshToken = RefreshToken.builder()
                .id(1L)
                .token(refreshTokenString)
                .user(user)
                .expiryDate(Instant.now().plusMillis(600000))
                .build();
    }

    @Test
    void register_shouldCallCreateUserAndLogin_whenRegistrationIsSuccessful() {

        // Given
        AuthResponse expectedAuthResponse = new AuthResponse(accessToken, refreshTokenString, user.getNickname());
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        when(userService.createUser(any(UserRequest.class))).thenReturn(null);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtService.generateAccessToken(any(Map.class), any(User.class))).thenReturn(accessToken);
        when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn(refreshToken);

        // When
        AuthResponse result = authenticationService.register(userRequest);

        // Then
        assertNotNull(result);
        assertEquals(expectedAuthResponse.accessToken(), result.accessToken());
        assertEquals(expectedAuthResponse.refreshToken(), result.refreshToken());

        verify(userService, times(1)).createUser(userRequest);
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(1)).generateAccessToken(any(Map.class), eq(user));
        verify(refreshTokenService, times(1)).createRefreshToken(user);
    }

    @Test
    void login_shouldReturnAuthResponse_whenCredentialsAreValid() {

        // Given
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtService.generateAccessToken(any(Map.class), any(User.class))).thenReturn(accessToken);
        when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn(refreshToken);

        // When
        AuthResponse result = authenticationService.login(loginRequest);

        // Then
        assertNotNull(result);
        assertEquals(accessToken, result.accessToken());
        assertEquals(refreshTokenString, result.refreshToken());
        assertEquals(user.getNickname(), result.nickname());

        verify(authenticationManager, times(1)).authenticate(
                argThat(token -> token.getName().equals(loginRequest.nickname()) && token.getCredentials().equals(loginRequest.password()))
        );
        verify(jwtService, times(1)).generateAccessToken(any(Map.class), eq(user));
        verify(refreshTokenService, times(1)).createRefreshToken(user);
    }

    @Test
    void login_shouldThrowBadCredentialsException_whenCredentialsAreInvalid() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        assertThrows(BadCredentialsException.class, () -> authenticationService.login(loginRequest));

        verify(jwtService, never()).generateAccessToken(any(Map.class), any(User.class));
        verify(refreshTokenService, never()).createRefreshToken(any(User.class));
    }

    @Test
    void refreshToken_shouldReturnNewAuthResponse_whenTokenIsValid() {

        // Given
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest(refreshTokenString);
        RefreshToken newRefreshToken = RefreshToken.builder().token("new-refresh-token").build();
        String newAccessToken = "new-access-token";

        when(refreshTokenService.findByToken(refreshTokenString)).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.verifyExpiration(refreshToken)).thenReturn(refreshToken);
        when(refreshTokenService.createRefreshToken(user)).thenReturn(newRefreshToken);
        when(jwtService.generateAccessToken(any(Map.class), eq(user))).thenReturn(newAccessToken);

        // When
        AuthResponse result = authenticationService.refreshToken(refreshTokenRequest);

        // Then
        assertNotNull(result);
        assertEquals(newAccessToken, result.accessToken());
        assertEquals(newRefreshToken.getToken(), result.refreshToken());

        verify(refreshTokenService, times(1)).findByToken(refreshTokenString);
        verify(refreshTokenService, times(1)).verifyExpiration(refreshToken);
        verify(refreshTokenService, times(1)).createRefreshToken(user);
        verify(jwtService, times(1)).generateAccessToken(any(Map.class), eq(user));
    }

    @Test
    void refreshToken_shouldThrowTokenRefreshException_whenTokenDoesNotExist() {

        // Given
        String nonExistentToken = "non-existent-token";
        RefreshTokenRequest request = new RefreshTokenRequest(nonExistentToken);
        when(refreshTokenService.findByToken(nonExistentToken)).thenReturn(Optional.empty());

        // When & Then
        TokenRefreshException exception = assertThrows(TokenRefreshException.class, () ->
                authenticationService.refreshToken(request));

        assertEquals("Failed for token [" + nonExistentToken + "]: Refresh token does not exists.", exception.getMessage());
        verify(refreshTokenService, never()).verifyExpiration(any());
    }

    @Test
    void refreshToken_shouldThrowTokenRefreshException_whenTokenIsExpired() {

        // Given
        RefreshTokenRequest request = new RefreshTokenRequest(refreshTokenString);
        when(refreshTokenService.findByToken(refreshTokenString)).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.verifyExpiration(refreshToken))
                .thenThrow(new TokenRefreshException(refreshTokenString, "Refresh token has expired."));

        // When & Then
        TokenRefreshException exception = assertThrows(TokenRefreshException.class, () ->
                authenticationService.refreshToken(request));

        assertEquals("Failed for token [dummy-refresh-token]: Refresh token has expired.", exception.getMessage());
        verify(refreshTokenService, never()).createRefreshToken(any());
    }

    @Test
    void logout_shouldCallDeleteByToken() {

        // Given
        RefreshTokenRequest request = new RefreshTokenRequest(refreshTokenString);
        doNothing().when(refreshTokenService).deleteByToken(refreshTokenString);

        // When
        authenticationService.logout(request);

        // Then
        verify(refreshTokenService, times(1)).deleteByToken(refreshTokenString);
    }
}
