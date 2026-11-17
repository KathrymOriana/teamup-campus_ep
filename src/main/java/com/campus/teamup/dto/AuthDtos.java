package com.campus.teamup.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public final class AuthDtos {

    private AuthDtos() { }

    private static final String PASSWORD_RULE = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$";
    private static final String PASSWORD_MESSAGE = "Usa al menos 8 caracteres, con letras y números.";

    public record SessionResponse(boolean authenticated, UserResponse user) { }

    public record LoginRequest(
            @NotBlank(message = "Ingresa tu correo.") String email,
            @NotBlank(message = "Ingresa tu contraseña.") String password) { }

    public record RegisterRequest(
            @NotBlank(message = "Ingresa tu nombre completo.") String nombre,
            @NotBlank(message = "Ingresa tu correo.") @Email(message = "Ingresa un correo válido.") String email,
            @Pattern(regexp = PASSWORD_RULE, message = PASSWORD_MESSAGE) String password) { }

    public record PasswordResetRequest(
            @NotBlank(message = "Ingresa tu correo.") @Email(message = "Ingresa un correo válido.") String email,
            @Pattern(regexp = PASSWORD_RULE, message = PASSWORD_MESSAGE) String password) { }
}
