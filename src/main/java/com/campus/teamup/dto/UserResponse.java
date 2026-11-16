package com.campus.teamup.dto;

import com.campus.teamup.model.User;

public record UserResponse(Long id, String nombre, String email, boolean foto) {

    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getNombre(), u.getEmail(), u.isFoto());
    }
}
