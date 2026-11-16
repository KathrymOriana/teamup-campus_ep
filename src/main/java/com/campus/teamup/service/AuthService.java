package com.campus.teamup.service;

import com.campus.teamup.dto.AuthDtos.PasswordResetRequest;
import com.campus.teamup.dto.AuthDtos.RegisterRequest;
import com.campus.teamup.dto.UserResponse;
import com.campus.teamup.exception.ApiException;
import com.campus.teamup.model.User;
import com.campus.teamup.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cuentas de usuario: registro, recuperación de contraseña y consulta del usuario actual. */
@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AuthService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        String nombre = request.nombre().trim();
        String hash = encoder.encode(request.password());

        User user = users.findByEmail(email).orElse(null);
        if (user == null) {
            user = users.save(new User(nombre, email, hash, false));
        } else if (user.hasPassword()) {
            throw ApiException.conflict("Ya existe una cuenta con ese correo. Inicia sesión.");
        } else {
            user.claim(nombre, hash); // la persona había sido invitada a un proyecto
        }
        return UserResponse.from(user);
    }

    /** Simulación académica: en una aplicación real se enviaría un enlace con un token al correo. */
    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        User user = users.findByEmail(request.email().trim().toLowerCase())
                .filter(User::hasPassword)
                .orElseThrow(() -> ApiException.notFound("No encontramos una cuenta con ese correo."));
        user.changePassword(encoder.encode(request.password()));
    }

    public UserResponse me(Long userId) {
        return users.findById(userId).map(UserResponse::from)
                .orElseThrow(() -> ApiException.notFound("Usuario no encontrado."));
    }
}
