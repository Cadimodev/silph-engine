package com.silphengine.infrastructure.repositories;

import com.silphengine.domain.entities.User;
import com.silphengine.domain.enums.Role;
import com.silphengine.infrastructure.AbstractRepositoryIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


public class UserRepositoryTest extends AbstractRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByNickname_shouldFindUser_whenUserExists() {

        // Given
        String nickname = "testuser";
        User user = User.builder()
                .nickname(nickname)
                .email("test@user.com")
                .password("Password1234!")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .collection(new ArrayList<>())
                .decks(new ArrayList<>())
                .build();

        userRepository.save(user);

        // When
        Optional<User> foundUser = userRepository.findByNickname(nickname);

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getNickname()).isEqualTo(nickname);
    }

    @Test
    void findByNickname_shouldReturnEmpty_whenUserDoesNotExists() {

        // When
        Optional<User> foundUser = userRepository.findByNickname("nonExistingNickname");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    void findByEmail_shouldFindUser_whenUserExists() {

        // Given
        String email = "test@user.com";
        User user = User.builder()
                .nickname("testuser")
                .email(email)
                .password("Password1234!")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .collection(new ArrayList<>())
                .decks(new ArrayList<>())
                .build();

        userRepository.save(user);

        // When
        Optional<User> foundUser = userRepository.findByEmail(email);

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo(email);
    }

    @Test
    void findByEmail_shouldReturnEmpty_whenUserDoesNotExists() {

        // When
        Optional<User> foundUser = userRepository.findByEmail("nonExistingEmail@test.com");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    void findByNicknameOrEmail_shouldFindUser_whenUserExists() {

        // Given
        String nickname = "testuser";
        String email = "test@user.com";
        User user = User.builder()
                .nickname(nickname)
                .email(email)
                .password("Password1234!")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .collection(new ArrayList<>())
                .decks(new ArrayList<>())
                .build();

        userRepository.save(user);

        // When
        Optional<User> foundUser = userRepository.findByNicknameOrEmail(nickname, email);

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getNickname()).isEqualTo(nickname);
        assertThat(foundUser.get().getEmail()).isEqualTo(email);
    }

    @Test
    void findByNicknameOrEmail_shouldReturnEmpty_whenUserDoesNotExists() {

        // When
        Optional<User> foundUser = userRepository.findByNicknameOrEmail("nonExistingNickname", "nonExistingEmail@test.com");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    void existsByNickname_shouldFindUser_whenUserExists() {

        // Given
        String nickname = "testuser";
        User user = User.builder()
                .nickname(nickname)
                .email("test@user.com")
                .password("Password1234!")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .collection(new ArrayList<>())
                .decks(new ArrayList<>())
                .build();

        userRepository.save(user);

        // When
        boolean exists = userRepository.existsByNickname(nickname);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByNickname_shouldReturnFalse_whenUserDoesNotExists() {

        // When
        boolean exists = userRepository.existsByNickname("nonExistingNickname");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void existsByEmail_shouldFindUser_whenUserExists() {

        // Given
        String email = "test@user.com";
        User user = User.builder()
                .nickname("testuser")
                .email(email)
                .password("Password1234!")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .collection(new ArrayList<>())
                .decks(new ArrayList<>())
                .build();

        userRepository.save(user);

        // When
        boolean exists = userRepository.existsByEmail(email);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_shouldReturnFalse_whenUserDoesNotExists() {

        // When
        boolean exists = userRepository.existsByEmail("nonExistingEmail@test.com");

        // Then
        assertThat(exists).isFalse();
    }
}
