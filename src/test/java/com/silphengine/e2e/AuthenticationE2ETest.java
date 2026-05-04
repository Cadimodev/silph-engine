package com.silphengine.e2e;

import com.silphengine.domain.dto.requests.LoginRequest;
import com.silphengine.domain.dto.requests.UserRequest;
import com.silphengine.domain.dto.responses.AuthResponse;
import com.silphengine.infrastructure.AbstractWebIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthenticationE2ETest extends AbstractWebIntegrationTest {

    @Autowired
    private RestTestClient client;

    @Test
    void registerFlow_ShouldAutoLoginAndGrantAccess() {

        // --- 1. Register
        UserRequest registerRequest = new UserRequest(
                "blue_champion",
                "blue@kanto.com",
                "Blastoise123!"
        );

        AuthResponse authResponse = client.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(registerRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(authResponse).isNotNull();
        String jwtToken = authResponse.accessToken();
        assertThat(jwtToken).isNotBlank();

        // --- 2. Access
        String userResponse = client.get()
                .uri("/api/v1/users/nickname/blue_champion")
                .headers(headers -> headers.setBearerAuth(jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertThat(userResponse).contains("blue@kanto.com");
    }

    @Test
    void loginFlow_ShouldGrantAccessToExistingUser() {

        // --- 1. Register
        UserRequest setupRequest = new UserRequest("lance_master", "lance@johto.com", "Dragonite123!");
        client.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(setupRequest)
                .exchange()
                .expectStatus().isOk();

        // --- 2. Login
        LoginRequest loginRequest = new LoginRequest("lance_master", "Dragonite123!");

        AuthResponse loginResponse = client.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(loginResponse).isNotNull();
        String jwtToken = loginResponse.accessToken();
        assertThat(jwtToken).isNotBlank();

        // --- 3. Access
        client.get()
                .uri("/api/v1/users/nickname/lance_master")
                .headers(headers -> headers.setBearerAuth(jwtToken))
                .exchange()
                .expectStatus().isOk();
    }
}
