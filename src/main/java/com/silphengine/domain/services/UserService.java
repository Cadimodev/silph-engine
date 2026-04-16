package com.silphengine.domain.services;

import com.silphengine.domain.dto.requests.PasswordChangeRequest;
import com.silphengine.domain.dto.requests.UserProfileRequest;
import com.silphengine.domain.dto.requests.UserRequest;
import com.silphengine.domain.dto.responses.UserResponse;

import java.util.UUID;

public interface UserService {

    UserResponse createUser(UserRequest userRequest);

    UserResponse getUserById(UUID id);

    UserResponse getUserByNickname(String nickname);

    UserResponse updateUserProfile(UUID id, UserProfileRequest userProfileRequest);

    void changeUserPassword(UUID id, PasswordChangeRequest passwordChangeRequest);

    void deleteUser(UUID id);
}
