package com.campus.teamup.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.campus.teamup.dto.NotificationDtos.Notification;
import com.campus.teamup.model.Project;
import com.campus.teamup.model.Task;
import com.campus.teamup.repository.ProjectRepository;
import com.campus.teamup.repository.TaskRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Avisos de la campana: se generan con las fechas, responsables y estados reales de las tareas. */
@Service
@Transactional(readOnly = true)
public class NotificationService {

    private final TaskRepository tasks;
    private final ProjectRepository projects;
    private final MembershipService membership;
    private final Clock clock;

    public NotificationService(TaskRepository tasks, ProjectRepository projects,
                               MembershipService membership, Clock clock) {
        this.tasks = tasks;
        this.projects = projects;
        this.membership = membership;
        this.clock = clock;
    }

    public List<Notification> forUser(Long userId) {
        List<Long> ids = membership.projectIdsOf(userId);
        Map<Long, String> names = projects.findAllById(ids).stream()
                .collect(Collectors.toMap(Project::getId, Project::getNombre));
        LocalDate today = LocalDate.now(clock);
        List<Task> open = tasks.findByProjectIdIn(ids).stream()
                .filter(t -> !t.isCompleted()).sorted(Comparator.comparing(Task::getFecha)).toList();

        List<Notification> out = new ArrayList<>();
        open.stream().filter(t -> t.isOverdue(today)).forEach(t -> out.add(new Notification(
                "danger", "exclamation-octagon",
                "«%s» está vencida (%s)".formatted(t.getTitulo(), names.get(t.getProjectId())), t.getProjectId())));
        open.stream().filter(t -> !t.isOverdue(today) && ChronoUnit.DAYS.between(today, t.getFecha()) <= 2)
                .forEach(t -> {
                    long d = ChronoUnit.DAYS.between(today, t.getFecha());
                    String when = d == 0 ? "hoy" : d == 1 ? "mañana" : "en 2 días";
                    out.add(new Notification("warn", "alarm", "«%s» vence %s".formatted(t.getTitulo(), when), t.getProjectId()));
                });
        open.stream().filter(t -> t.getResponsableId() == null).forEach(t -> out.add(new Notification(
                "warn", "person-dash",
                "«%s» no tiene responsable (%s)".formatted(t.getTitulo(), names.get(t.getProjectId())), t.getProjectId())));
        return out;
    }
}
