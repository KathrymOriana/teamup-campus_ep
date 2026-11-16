package com.campus.teamup.controller;

import com.campus.teamup.dto.ProgressDtos.ProgressReport;
import com.campus.teamup.model.AuthUser;
import com.campus.teamup.service.ProgressService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private final ProgressService service;

    public ProgressController(ProgressService service) {
        this.service = service;
    }

    @GetMapping
    public ProgressReport report(@RequestParam(required = false) Long projectId,
                                 @AuthenticationPrincipal AuthUser user) {
        return service.report(user.getId(), projectId);
    }
}
