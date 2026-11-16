package com.campus.teamup.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.campus.teamup.dto.ProgressDtos.Health;
import com.campus.teamup.dto.ProgressDtos.ProgressReport;
import com.campus.teamup.dto.ProgressDtos.ProjectProgress;
import com.campus.teamup.dto.ProgressDtos.Stats;
import com.campus.teamup.dto.ProgressDtos.Team;
import com.campus.teamup.dto.ProgressDtos.WeeklySeries;
import com.campus.teamup.dto.UserResponse;
import com.campus.teamup.model.Priority;
import com.campus.teamup.model.Project;
import com.campus.teamup.model.Task;
import com.campus.teamup.model.TaskStatus;
import com.campus.teamup.repository.ProjectRepository;
import com.campus.teamup.repository.TaskRepository;
import com.campus.teamup.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Indicadores del proyecto: todo se calcula a partir de las tareas reales, nunca se guarda ni se escribe a mano. */
@Service
@Transactional(readOnly = true)
public class ProgressService {

    private static final int WEEKS = 6;

    private final TaskRepository tasks;
    private final ProjectRepository projects;
    private final MembershipService membership;
    private final UserRepository users;
    private final Clock clock;

    public ProgressService(TaskRepository tasks, ProjectRepository projects,
                           MembershipService membership, UserRepository users, Clock clock) {
        this.tasks = tasks;
        this.projects = projects;
        this.membership = membership;
        this.users = users;
        this.clock = clock;
    }

    /** Informe de un proyecto, o de todos los del usuario cuando projectId es nulo. */
    public ProgressReport report(Long userId, Long projectId) {
        if (projectId != null) {
            membership.requireMember(projectId, userId);
        }
        List<Long> myIds = membership.projectIdsOf(userId);
        List<Project> mine = projects.findAllById(myIds);
        List<Long> scopeIds = projectId == null ? myIds : List.of(projectId);
        List<Project> scope = mine.stream().filter(p -> scopeIds.contains(p.getId())).toList();
        List<Task> scopeTasks = tasks.findByProjectIdIn(scopeIds);

        var tasksByProject = tasks.findByProjectIdIn(myIds).stream()
                .collect(java.util.stream.Collectors.groupingBy(Task::getProjectId));
        List<ProjectProgress> perProject = mine.stream()
                .map(p -> {
                    List<Task> t = tasksByProject.getOrDefault(p.getId(), List.of());
                    return new ProjectProgress(p.getId(), p.getNombre(), p.getColor(),
                            percent((int) t.stream().filter(Task::isCompleted).count(), t.size()));
                }).toList();

        return new ProgressReport(projectId, stats(scopeTasks), weeklySeries(scopeTasks),
                perProject, team(scopeIds, scopeTasks), health(scope, scopeTasks));
    }

    public Stats stats(List<Task> list) {
        LocalDate today = LocalDate.now(clock);
        int total = list.size();
        int done = count(list, TaskStatus.COMPLETADA);
        int doing = count(list, TaskStatus.EN_PROGRESO);
        int pending = count(list, TaskStatus.PENDIENTE);
        Map<String, Integer> priorities = new LinkedHashMap<>();
        for (Priority p : Priority.values()) {
            priorities.put(p.label(), (int) list.stream().filter(t -> t.getPrioridad() == p).count());
        }
        return new Stats(total, done, doing, pending,
                (int) list.stream().filter(t -> t.isOverdue(today)).count(),
                percent(done, total), percent(doing, total), percent(pending, total), priorities);
    }

    /** Estado de salud: tareas vencidas, tareas sin responsable y entregas muy cercanas. */
    public Health health(List<Project> projectList, List<Task> projectTasks) {
        LocalDate today = LocalDate.now(clock);
        List<Task> open = projectTasks.stream().filter(t -> !t.isCompleted()).toList();
        long overdue = open.stream().filter(t -> t.isOverdue(today)).count();
        long unassigned = open.stream().filter(t -> t.getResponsableId() == null).count();

        List<String> reasons = new ArrayList<>();
        if (overdue > 0) {
            reasons.add(overdue + (overdue == 1 ? " tarea vencida" : " tareas vencidas"));
        }
        if (unassigned > 0) {
            reasons.add(unassigned + (unassigned == 1 ? " tarea sin responsable" : " tareas sin responsable"));
        }
        projectList.stream()
                .filter(p -> !p.getEntrega().isBefore(today) && ChronoUnit.DAYS.between(today, p.getEntrega()) <= 2)
                .filter(p -> projectTasks.stream().anyMatch(t -> t.getProjectId().equals(p.getId()) && !t.isCompleted()))
                .forEach(p -> {
                    long days = ChronoUnit.DAYS.between(today, p.getEntrega());
                    reasons.add("Próxima entrega: " + p.getNombre() + (days == 0 ? " (hoy)" : " (en " + days + " días)"));
                });
        return new Health(!reasons.isEmpty(), reasons);
    }

    public int percent(int part, int total) {
        return total == 0 ? 0 : Math.round(100f * part / total);
    }

    /* ---------- apoyo ---------- */

    /** % acumulado de tareas completadas al cierre de cada una de las últimas 6 semanas. */
    private WeeklySeries weeklySeries(List<Task> list) {
        LocalDate today = LocalDate.now(clock);
        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        for (int i = WEEKS - 1; i >= 0; i--) {
            LocalDate end = today.minusWeeks(i);
            labels.add("Semana " + (WEEKS - i));
            int done = (int) list.stream()
                    .filter(t -> t.getCompletadaEn() != null && !t.getCompletadaEn().isAfter(end)).count();
            values.add(percent(done, list.size()));
        }
        return new WeeklySeries(labels, values);
    }

    private Team team(List<Long> scopeIds, List<Task> scopeTasks) {
        Set<Long> memberIds = new LinkedHashSet<>();
        scopeIds.forEach(id -> memberIds.addAll(membership.memberIdsOf(id)));
        int active = (int) memberIds.stream()
                .filter(uid -> scopeTasks.stream().anyMatch(t -> uid.equals(t.getResponsableId()) && !t.isCompleted()))
                .count();
        return new Team(users.findAllById(memberIds).stream().map(UserResponse::from).toList(), active);
    }

    private static int count(List<Task> list, TaskStatus status) {
        return (int) list.stream().filter(t -> t.getEstado() == status).count();
    }
}
