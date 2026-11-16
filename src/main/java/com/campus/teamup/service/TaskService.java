package com.campus.teamup.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.campus.teamup.dto.TaskDtos.TaskRequest;
import com.campus.teamup.dto.TaskDtos.TaskResponse;
import com.campus.teamup.exception.ApiException;
import com.campus.teamup.model.Member;
import com.campus.teamup.model.Task;
import com.campus.teamup.model.TaskStatus;
import com.campus.teamup.repository.TaskRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso de tareas y sus reglas de permisos:
 * - El administrador del proyecto crea, asigna, edita y elimina cualquier tarea.
 * - Un integrante solo puede crear tareas para sí mismo y editar o completar las que le asignaron.
 */
@Service
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository tasks;
    private final MembershipService membership;
    private final Clock clock;

    public TaskService(TaskRepository tasks, MembershipService membership, Clock clock) {
        this.tasks = tasks;
        this.membership = membership;
        this.clock = clock;
    }

    /** Tareas de un proyecto, o de todos los proyectos del usuario cuando projectId es nulo. */
    public List<TaskResponse> list(Long userId, Long projectId) {
        List<Long> ids;
        if (projectId != null) {
            membership.requireMember(projectId, userId);
            ids = List.of(projectId);
        } else {
            ids = membership.projectIdsOf(userId);
        }
        Set<Long> adminOf = membership.adminProjectIdsOf(userId);
        LocalDate today = today();
        return tasks.findByProjectIdIn(ids).stream()
                .sorted(Comparator.comparing(Task::getFecha).thenComparing(Task::getId))
                .map(t -> TaskResponse.from(t, today, canEdit(t, userId, adminOf)))
                .toList();
    }

    @Transactional
    public TaskResponse create(Long userId, TaskRequest request) {
        Member me = membership.requireMember(request.projectId(), userId);
        validateAssignee(request.projectId(), request.responsableId(), userId, me.isAdmin());
        Task task = new Task(request.projectId(), request.titulo().trim(), textOrDefault(request.descripcion()),
                request.responsableId(), request.prioridad(), request.fecha(), request.estado(), null);
        if (request.estado() == TaskStatus.COMPLETADA) {
            task.changeState(TaskStatus.COMPLETADA, today());
        }
        return TaskResponse.from(tasks.save(task), today(), true);
    }

    @Transactional
    public TaskResponse update(Long taskId, Long userId, TaskRequest request) {
        Task task = find(taskId);
        Member me = requireEditor(task, userId);
        validateAssignee(task.getProjectId(), request.responsableId(), userId, me.isAdmin());
        task.edit(request.titulo().trim(), textOrDefault(request.descripcion()), request.responsableId(),
                request.prioridad(), request.fecha(), request.estado(), today());
        return TaskResponse.from(task, today(), true);
    }

    @Transactional
    public TaskResponse changeState(Long taskId, TaskStatus newState, Long userId) {
        Task task = find(taskId);
        requireEditor(task, userId);
        task.changeState(newState, today());
        return TaskResponse.from(task, today(), true);
    }

    @Transactional
    public void delete(Long taskId, Long userId) {
        Task task = find(taskId);
        requireEditor(task, userId);
        tasks.delete(task);
    }

    /* ---------- reglas ---------- */

    private Member requireEditor(Task task, Long userId) {
        Member me = membership.requireMember(task.getProjectId(), userId);
        if (!me.isAdmin() && !userId.equals(task.getResponsableId())) {
            throw ApiException.forbidden("Solo el responsable o el administrador pueden modificar esta tarea.");
        }
        return me;
    }

    private boolean canEdit(Task task, Long userId, Set<Long> adminProjects) {
        return adminProjects.contains(task.getProjectId()) || userId.equals(task.getResponsableId());
    }

    private void validateAssignee(Long projectId, Long assigneeId, Long userId, boolean admin) {
        if (!admin) {
            if (!userId.equals(assigneeId)) {
                throw ApiException.forbidden("Solo el administrador puede asignar tareas a otras personas.");
            }
            return;
        }
        if (assigneeId != null && !membership.isMember(projectId, assigneeId)) {
            throw ApiException.invalid("El responsable debe ser integrante del proyecto.");
        }
    }

    private Task find(Long taskId) {
        return tasks.findById(taskId).orElseThrow(() -> ApiException.notFound("Tarea no encontrada."));
    }

    private LocalDate today() { return LocalDate.now(clock); }

    private static String textOrDefault(String text) {
        return text == null || text.isBlank() ? "Sin descripción." : text.trim();
    }
}
