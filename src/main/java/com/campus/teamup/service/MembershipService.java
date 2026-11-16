package com.campus.teamup.service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.campus.teamup.dto.ProjectDtos.MemberResponse;
import com.campus.teamup.dto.ProjectDtos.AddMemberRequest;
import com.campus.teamup.exception.ApiException;
import com.campus.teamup.model.Member;
import com.campus.teamup.model.Role;
import com.campus.teamup.model.User;
import com.campus.teamup.repository.MemberRepository;
import com.campus.teamup.repository.ProjectRepository;
import com.campus.teamup.repository.TaskRepository;
import com.campus.teamup.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reglas de acceso: quién pertenece a un proyecto y quién puede administrarlo. Todas las demás capas consultan aquí. */
@Service
@Transactional(readOnly = true)
public class MembershipService {

    private final MemberRepository members;
    private final ProjectRepository projects;
    private final UserRepository users;
    private final TaskRepository tasks;

    public MembershipService(MemberRepository members, ProjectRepository projects,
                             UserRepository users, TaskRepository tasks) {
        this.members = members;
        this.projects = projects;
        this.users = users;
        this.tasks = tasks;
    }

    public Member requireMember(Long projectId, Long userId) {
        if (!projects.existsById(projectId)) {
            throw ApiException.notFound("Proyecto no encontrado.");
        }
        return members.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> ApiException.forbidden("No perteneces a este proyecto."));
    }

    public Member requireAdmin(Long projectId, Long userId) {
        Member member = requireMember(projectId, userId);
        if (!member.isAdmin()) {
            throw ApiException.forbidden("Solo el administrador del proyecto puede hacer esto.");
        }
        return member;
    }

    public List<Long> projectIdsOf(Long userId) {
        return members.findByUserId(userId).stream().map(Member::getProjectId).toList();
    }

    public Set<Long> adminProjectIdsOf(Long userId) {
        return members.findByUserId(userId).stream().filter(Member::isAdmin)
                .map(Member::getProjectId).collect(Collectors.toSet());
    }

    public boolean isMember(Long projectId, Long userId) {
        return members.findByProjectIdAndUserId(projectId, userId).isPresent();
    }

    public List<MemberResponse> list(Long projectId, Long actorId) {
        requireMember(projectId, actorId);
        List<Member> list = members.findByProjectId(projectId);
        var usersById = users.findAllById(list.stream().map(Member::getUserId).toList()).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        return list.stream()
                .map(m -> {
                    User u = usersById.get(m.getUserId());
                    return new MemberResponse(u.getId(), u.getNombre(), u.getEmail(), u.isFoto(), m.getRol());
                })
                .toList();
    }

    @Transactional
    public MemberResponse add(Long projectId, AddMemberRequest request, Long actorId) {
        requireAdmin(projectId, actorId);
        String email = request.email().trim().toLowerCase();
        User user = users.findByEmail(email).orElse(null);
        if (user == null) {
            if (request.nombre() == null || request.nombre().isBlank()) {
                throw ApiException.invalid("Indica el nombre del nuevo integrante.");
            }
            user = users.save(new User(request.nombre().trim(), email, null, false));
        }
        if (members.findByProjectIdAndUserId(projectId, user.getId()).isPresent()) {
            throw ApiException.conflict(user.getNombre() + " ya es integrante de este proyecto.");
        }
        Member member = members.save(new Member(projectId, user.getId(), request.rol()));
        return new MemberResponse(user.getId(), user.getNombre(), user.getEmail(), user.isFoto(), member.getRol());
    }

    @Transactional
    public void changeRole(Long projectId, Long targetUserId, Role rol, Long actorId) {
        requireAdmin(projectId, actorId);
        Member target = members.findByProjectIdAndUserId(projectId, targetUserId)
                .orElseThrow(() -> ApiException.notFound("Esa persona no es integrante del proyecto."));
        if (target.isAdmin() && rol != Role.ADMIN && members.countByProjectIdAndRol(projectId, Role.ADMIN) == 1) {
            throw ApiException.conflict("El proyecto debe tener al menos un administrador.");
        }
        target.setRol(rol);
    }

    @Transactional
    public void remove(Long projectId, Long targetUserId, Long actorId) {
        requireAdmin(projectId, actorId);
        Member target = members.findByProjectIdAndUserId(projectId, targetUserId)
                .orElseThrow(() -> ApiException.notFound("Esa persona no es integrante del proyecto."));
        if (target.isAdmin() && members.countByProjectIdAndRol(projectId, Role.ADMIN) == 1) {
            throw ApiException.conflict("No puedes quitar al único administrador.");
        }
        members.delete(target);
        tasks.unassign(projectId, targetUserId); // sus tareas quedan sin responsable
    }

    /** Alta del creador como administrador al crear un proyecto. */
    @Transactional
    public void addCreator(Long projectId, Long userId) {
        members.save(new Member(projectId, userId, Role.ADMIN));
    }

    public Collection<Long> memberIdsOf(Long projectId) {
        return members.findByProjectId(projectId).stream().map(Member::getUserId).toList();
    }
}
