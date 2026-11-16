package com.campus.teamup.service;

import com.campus.teamup.model.AuthUser;
import com.campus.teamup.model.User;
import com.campus.teamup.repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository users;

    public AppUserDetailsService(UserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = users.findByEmail(email.trim().toLowerCase())
                .filter(User::hasPassword)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        return new AuthUser(user.getId(), user.getNombre(), user.getEmail(), user.getPasswordHash());
    }
}
