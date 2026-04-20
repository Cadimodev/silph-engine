package com.silphengine.application.mappers;

import com.silphengine.domain.dto.requests.UserProfileRequest;
import com.silphengine.domain.dto.requests.UserRequest;
import com.silphengine.domain.dto.responses.UserResponse;
import com.silphengine.domain.enums.Role;
import com.silphengine.domain.entities.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(UserRequest userRequest, String encodedPassword) {
       return  User.builder()
               .nickname(userRequest.nickname())
               .email(userRequest.email())
               .password(encodedPassword)
               .role(Role.USER)
               .build();
    }

    public UserResponse toResponse(User user) {

        return new UserResponse(
                user.getId(),
                user.getNickname(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }

    public void updateEntityFromRequest(User user, UserProfileRequest userProfileRequest) {
        user.updateProfile(userProfileRequest.nickname(), userProfileRequest.email());
    }
}
