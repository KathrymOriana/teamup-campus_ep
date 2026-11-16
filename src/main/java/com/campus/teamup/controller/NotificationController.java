package com.campus.teamup.controller;

import java.util.List;

import com.campus.teamup.dto.NotificationDtos.Notification;
import com.campus.teamup.model.AuthUser;
import com.campus.teamup.service.NotificationService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    public List<Notification> list(@AuthenticationPrincipal AuthUser user) {
        return service.forUser(user.getId());
    }
}
