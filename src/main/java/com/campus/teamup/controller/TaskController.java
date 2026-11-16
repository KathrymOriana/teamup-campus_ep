package com.campus.teamup.controller;

import java.util.List;

import com.campus.teamup.dto.TaskDtos.StateRequest;
import com.campus.teamup.dto.TaskDtos.TaskRequest;
import com.campus.teamup.dto.TaskDtos.TaskResponse;
import com.campus.teamup.model.AuthUser;
import com.campus.teamup.service.TaskService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @GetMapping
    public List<TaskResponse> list(@RequestParam(required = false) Long projectId,
                                   @AuthenticationPrincipal AuthUser user) {
        return service.list(user.getId(), projectId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@Valid @RequestBody TaskRequest body, @AuthenticationPrincipal AuthUser user) {
        return service.create(user.getId(), body);
    }

    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable Long id, @Valid @RequestBody TaskRequest body,
                               @AuthenticationPrincipal AuthUser user) {
        return service.update(id, user.getId(), body);
    }

    @PatchMapping("/{id}/state")
    public TaskResponse changeState(@PathVariable Long id, 
                                    @Valid @RequestBody StateRequest body,
                                    @AuthenticationPrincipal AuthUser user) {
        return service.changeState(id, body.estado(), user.getId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal AuthUser user) {
        service.delete(id, user.getId());
    }
}
