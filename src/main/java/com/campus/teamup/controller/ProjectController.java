package com.campus.teamup.controller;

import java.util.List;

import com.campus.teamup.dto.ProjectDtos.ProjectRequest;
import com.campus.teamup.dto.ProjectDtos.ProjectResponse;
import com.campus.teamup.dto.ProjectDtos.ProjectSummary;
import com.campus.teamup.model.AuthUser;
import com.campus.teamup.service.ProjectService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProjectResponse> list(@AuthenticationPrincipal AuthUser user) {
        return service.list(user.getId());
    }

    @GetMapping("/summary")
    public ProjectSummary summary(@AuthenticationPrincipal AuthUser user) {
        return service.summary(user.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@Valid @RequestBody ProjectRequest body, @AuthenticationPrincipal AuthUser user) {
        return service.create(user.getId(), body);
    }

    @PutMapping("/{id}")
    public ProjectResponse update(@PathVariable Long id, @Valid @RequestBody ProjectRequest body,
                                  @AuthenticationPrincipal AuthUser user) {
        return service.update(id, user.getId(), body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal AuthUser user) {
        service.delete(id, user.getId());
    }
}
