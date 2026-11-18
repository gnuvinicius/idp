package io.garage.manifest.service;

import io.garage.auth.User;
import io.garage.auth.UserRoleEnum;

public record ApplyContext(
        Long userId,
        String username,
        String email,
        UserRoleEnum role,
        User userEntity
) {
    public boolean isAdmin() {
        return role == UserRoleEnum.ADMIN;
    }
}
