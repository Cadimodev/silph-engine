package com.silphengine.security;

import com.silphengine.domain.entities.User;
import com.silphengine.domain.enums.Role;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private User user;

    // Use a secure key for testing (at least 256 bits)
    private final String testSecretKey = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long testExpiration = 1000 * 60 * 60 * 24; // 1 day

    @BeforeEach
    void setUp() {
        
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", testSecretKey);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", testExpiration);

        user = User.builder()
                .id(UUID.randomUUID())
                .nickname("testUser")
                .email("test@email.com")
                .password("password123")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void generateAccessToken_shouldGenerateValidToken() {
        // When
        String token = jwtService.generateAccessToken(user);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // Verify we can extract the username from it
        String extractedUsername = jwtService.extractUsername(token);
        assertEquals(user.getUsername(), extractedUsername);
    }

    @Test
    void generateAccessTokenWithExtraClaims_shouldIncludeClaims() {

        // Given
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("email", user.getEmail());

        // When
        String token = jwtService.generateAccessToken(extraClaims, user);

        // Then
        assertNotNull(token);
        String extractedUsername = jwtService.extractUsername(token);
        assertEquals(user.getUsername(), extractedUsername);
        
        // Extract a specific custom claim
        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));
        assertEquals("USER", role);
        
        String email = jwtService.extractClaim(token, claims -> claims.get("email", String.class));
        assertEquals(user.getEmail(), email);
    }

    @Test
    void isTokenValid_shouldReturnTrue_whenTokenIsValidAndUsernameMatches() {

        // Given
        String token = jwtService.generateAccessToken(user);

        // When
        boolean isValid = jwtService.isTokenValid(token, user);

        // Then
        assertTrue(isValid);
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenUsernameDoesNotMatch() {

        // Given
        String token = jwtService.generateAccessToken(user);
        
        User differentUser = User.builder()
                .id(UUID.randomUUID())
                .nickname("differentUser")
                .email("different@email.com")
                .password("password123")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        // When
        boolean isValid = jwtService.isTokenValid(token, differentUser);

        // Then
        assertFalse(isValid);
    }

    @Test
    void isTokenValid_shouldThrowException_whenTokenIsExpired() {
        // Given
        byte[] keyBytes = Decoders.BASE64.decode(testSecretKey);
        Key key = Keys.hmacShaKeyFor(keyBytes);
        
        String expiredToken = Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 2)) // 2 hours ago
                .setExpiration(new Date(System.currentTimeMillis() - 1000 * 60 * 60)) // Expired 1 hour ago
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // When & Then
        assertThrows(ExpiredJwtException.class, () -> jwtService.isTokenValid(expiredToken, user));
        assertThrows(ExpiredJwtException.class, () -> jwtService.extractUsername(expiredToken));
    }
}