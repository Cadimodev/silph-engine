package com.silphengine.infrastructure.web.controllers;

import com.silphengine.domain.dto.requests.PasswordChangeRequest;
import com.silphengine.domain.dto.requests.UserProfileRequest;
import com.silphengine.domain.dto.requests.UserRequest;
import com.silphengine.domain.dto.responses.UserResponse;
import com.silphengine.domain.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {

        UserResponse response = userService.createUser(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {

        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/nickname/{nickname}")
    public ResponseEntity<UserResponse> getUserByNickname(@PathVariable String nickname) {

        return ResponseEntity.ok(userService.getUserByNickname(nickname));
    }

    @PatchMapping("/{id}/profile")
    public ResponseEntity<UserResponse> updateUserProfile(@PathVariable UUID id, @Valid @RequestBody UserProfileRequest request) {

        return ResponseEntity.ok(userService.updateUserProfile(id, request));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> changeUserPassword(@PathVariable UUID id, @Valid @RequestBody PasswordChangeRequest request) {

        userService.changeUserPassword(id, request);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {

        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
