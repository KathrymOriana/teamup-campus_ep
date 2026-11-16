package com.campus.teamup.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.campus.teamup.dto.ProgressDtos.Health;
import com.campus.teamup.dto.ProjectDtos.ProjectRequest;
import com.campus.teamup.dto.ProjectDtos.ProjectResponse;
import com.campus.teamup.dto.ProjectDtos.ProjectSummary;
import com.campus.teamup.dto.UserResponse;
import com.campus.teamup.exception.ApiException;
import com.campus.teamup.model.Course;
import com.campus.teamup.model.Member;
import com.campus.teamup.model.Project;
import com.campus.teamup.model.ProjectStatus;
import com.campus.teamup.model.Role;
import com.campus.teamup.model.Task;
import com.campus.teamup.model.User;
import com.campus.teamup.repository.CourseRepository;
import com.campus.teamup.repository.MemberRepository;
import com.campus.teamup.repository.ProjectRepository;
import com.campus.teamup.repository.TaskRepository;
import com.campus.teamup.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Casos de uso de proyectos. El avance, el atraso y las alertas se calculan aquí, nunca en el navegador. */
@Service
@Transactional(readOnly = true)
public class ProjectService {

    private static final List<String> COLORS = List.of("purple", "green", "blue", "orange");

    private final ProjectRepository projects;
    private final MemberRepository members;
    private final TaskRepository tasks;
    private final CourseRepository courses;
    private final UserRepository users;
    private final MembershipService membership;
    private final ProgressService progress;
    private final Clock clock;

    public ProjectService(ProjectRepository projects, MemberRepository members, TaskRepository tasks,
                          CourseRepository courses, UserRepository users, MembershipService membership,
                          ProgressService progress, Clock clock) {
        this.projects = projects;
        this.members = members;
        this.tasks = tasks;
        this.courses = courses;
        this.users = users;
        this.membership = membership;
        this.progress = progress;
        this.clock = clock;
    }

    public List<ProjectResponse> list(Long userId) {
        List<Project> mine = projects.findAllById(membership.projectIdsOf(userId)).stream()
                .sorted(Comparator.comparing(Project::getId).reversed()).toList();
        return toResponses(mine, userId);
    }

    public ProjectSummary summary(Long userId) {
        List<ProjectResponse> list = list(userId);
        return new ProjectSummary(
                list.size(),
                (int) list.stream().filter(p -> p.estado() == ProjectStatus.EN_PROGRESO).count(),
                (int) list.stream().filter(p -> p.estado() != ProjectStatus.PENDIENTE).count(),
                (int) list.stream().filter(p -> p.estado() == ProjectStatus.PENDIENTE).count(),
                (int) list.stream().filter(ProjectResponse::atrasado).count());
    }

    @Transactional
    public ProjectResponse create(Long userId, ProjectRequest request) {
        requireCourse(request.cursoId());
        String color = COLORS.get((int) (projects.count() % COLORS.size()));
        Project project = projects.save(new Project(request.nombre().trim(), request.cursoId(),
                blankToDefault(request.descripcion()), request.entrega(), request.estado(), color));
        membership.addCreator(project.getId(), userId);
        return toResponses(List.of(project), userId).getFirst();
    }

    @Transactional
    public ProjectResponse update(Long projectId, Long userId, ProjectRequest request) {
        membership.requireAdmin(projectId, userId);
        requireCourse(request.cursoId());
        Project project = projects.findById(projectId).orElseThrow(() -> ApiException.notFound("Proyecto no encontrado."));
        project.update(request.nombre().trim(), request.cursoId(), blankToDefault(request.descripcion()),
                request.entrega(), request.estado());
        return toResponses(List.of(project), userId).getFirst();
    }

    @Transactional
    public void delete(Long projectId, Long userId) {
        membership.requireAdmin(projectId, userId);
        tasks.deleteByProjectId(projectId);
        members.deleteByProjectId(projectId);
        projects.deleteById(projectId);
    }

    /* ---------- apoyo ---------- */

    private List<ProjectResponse> toResponses(List<Project> list, Long userId) {
        if (list.isEmpty()) {
            return List.of();
        }
        List<Long> ids = list.stream().map(Project::getId).toList();
        Map<Long, List<Task>> tasksByProject = tasks.findByProjectIdIn(ids).stream()
                .collect(Collectors.groupingBy(Task::getProjectId));
        Map<Long, List<Member>> membersByProject = members.findByProjectIdIn(ids).stream()
                .collect(Collectors.groupingBy(Member::getProjectId));
        Map<Long, String> courseNames = courses.findAll().stream()
                .collect(Collectors.toMap(Course::getId, Course::getNombre));
        Map<Long, User> usersById = users.findAllById(
                        membersByProject.values().stream().flatMap(List::stream).map(Member::getUserId).distinct().toList())
                .stream().collect(Collectors.toMap(User::getId, u -> u));
        LocalDate today = LocalDate.now(clock);

        return list.stream().map(p -> {
            List<Task> projectTasks = tasksByProject.getOrDefault(p.getId(), List.of());
            List<Member> projectMembers = membersByProject.getOrDefault(p.getId(), List.of());
            int done = (int) projectTasks.stream().filter(Task::isCompleted).count();
            int pct = progress.percent(done, projectTasks.size());
            Health health = progress.health(List.of(p), projectTasks);
            Role myRole = projectMembers.stream().filter(m -> m.getUserId().equals(userId))
                    .map(Member::getRol).findFirst().orElse(Role.MIEMBRO);
            return new ProjectResponse(p.getId(), p.getNombre(), p.getCursoId(),
                    courseNames.getOrDefault(p.getCursoId(), ""), p.getDescripcion(), p.getEntrega(),
                    p.getEstado(), p.getColor(), myRole,
                    projectMembers.stream().map(m -> UserResponse.from(usersById.get(m.getUserId()))).toList(),
                    projectTasks.size(), done, pct,
                    p.getEntrega().isBefore(today) && pct < 100,
                    health.atencion(), health.motivos());
        }).toList();
    }

    private void requireCourse(Long courseId) {
        if (!courses.existsById(courseId)) {
            throw ApiException.invalid("El curso elegido no existe.");
        }
    }

    private static String blankToDefault(String text) {
        return text == null || text.isBlank() ? "Sin descripción." : text.trim();
    }
}
