package com.campus.teamup.controller;

import java.util.List;

import com.campus.teamup.dto.ProjectDtos.AddMemberRequest;
import com.campus.teamup.dto.ProjectDtos.MemberResponse;
import com.campus.teamup.dto.ProjectDtos.RoleRequest;
import com.campus.teamup.model.AuthUser;
import com.campus.teamup.service.MembershipService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
public class MemberController {

    private final MembershipService service;

    public MemberController(MembershipService service) {
        this.service = service;
    }

    @GetMapping
    public List<MemberResponse> list(@PathVariable Long projectId, @AuthenticationPrincipal AuthUser user) {
        return service.list(projectId, user.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse add(@PathVariable Long projectId, @Valid @RequestBody AddMemberRequest body,
                              @AuthenticationPrincipal AuthUser user) {
        return service.add(projectId, body, user.getId());
    }

    @PatchMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeRole(@PathVariable Long projectId, @PathVariable Long userId,
                           @Valid @RequestBody RoleRequest body, @AuthenticationPrincipal AuthUser user) {
        service.changeRole(projectId, userId, body.rol(), user.getId());
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long projectId, @PathVariable Long userId,
                       @AuthenticationPrincipal AuthUser user) {
        service.remove(projectId, userId, user.getId());
    }
}
