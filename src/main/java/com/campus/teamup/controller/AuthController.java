package com.campus.teamup.controller;

import com.campus.teamup.dto.AuthDtos.LoginRequest;
import com.campus.teamup.dto.AuthDtos.PasswordResetRequest;
import com.campus.teamup.dto.AuthDtos.RegisterRequest;
import com.campus.teamup.dto.AuthDtos.SessionResponse;
import com.campus.teamup.dto.UserResponse;
import com.campus.teamup.model.AuthUser;
import com.campus.teamup.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Capa web de autenticación: traduce HTTP y delega las reglas en AuthService. (El cierre de sesión lo gestiona Spring Security.) */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository contextRepository;
    private final SecurityContextHolderStrategy contextHolder = SecurityContextHolder.getContextHolderStrategy();

    public AuthController(AuthService service, AuthenticationManager authenticationManager,
                          SecurityContextRepository contextRepository) {
        this.service = service;
        this.authenticationManager = authenticationManager;
        this.contextRepository = contextRepository;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest body,
                                 HttpServletRequest request, HttpServletResponse response) {
        UserResponse created = service.register(body);
        startSession(body.email(), body.password(), request, response); // la cuenta nueva entra directamente
        return created;
    }

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest body,
                              HttpServletRequest request, HttpServletResponse response) {
        AuthUser user = startSession(body.email(), body.password(), request, response);
        return service.me(user.getId());
    }

    /** Consulta pública del estado de sesión (200 con o sin usuario); la usan las páginas de acceso. */
    @GetMapping("/session")
    public SessionResponse session(@AuthenticationPrincipal AuthUser user) {
        return user == null ? new SessionResponse(false, null) : new SessionResponse(true, service.me(user.getId()));
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AuthUser user) {
        return service.me(user.getId());
    }

    @PostMapping("/password-reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody PasswordResetRequest body) {
        service.resetPassword(body);
    }

    private AuthUser startSession(String email, String password,
                                  HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(email.trim().toLowerCase(), password));
        if (request.getSession(false) != null) {
            request.changeSessionId(); // evita la fijación de sesión
        }
        SecurityContext context = contextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        contextHolder.setContext(context);
        contextRepository.saveContext(context, request, response);
        return (AuthUser) authentication.getPrincipal();
    }
}
