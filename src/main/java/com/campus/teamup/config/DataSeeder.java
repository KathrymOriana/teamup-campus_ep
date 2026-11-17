package com.campus.teamup.config;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import com.campus.teamup.model.Course;
import com.campus.teamup.model.Member;
import com.campus.teamup.model.Priority;
import com.campus.teamup.model.Project;
import com.campus.teamup.model.ProjectStatus;
import com.campus.teamup.model.Role;
import com.campus.teamup.model.Task;
import com.campus.teamup.model.TaskStatus;
import com.campus.teamup.model.User;
import com.campus.teamup.repository.CourseRepository;
import com.campus.teamup.repository.MemberRepository;
import com.campus.teamup.repository.ProjectRepository;
import com.campus.teamup.repository.TaskRepository;
import com.campus.teamup.repository.UserRepository;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Datos de ejemplo para la demostración. Solo se cargan si la base está vacía.
 * Cuentas de prueba: ana.lopez@campus.edu, carlos.rojas@campus.edu... (contraseña: teamup123).
 */
@Component
class DataSeeder implements ApplicationRunner {

    private static final String DEMO_PASSWORD = "teamup123";

    private final UserRepository users;
    private final CourseRepository courses;
    private final ProjectRepository projects;
    private final MemberRepository members;
    private final TaskRepository tasks;
    private final PasswordEncoder encoder;
    private final Clock clock;

    DataSeeder(UserRepository users, CourseRepository courses, ProjectRepository projects,
               MemberRepository members, TaskRepository tasks, PasswordEncoder encoder, Clock clock) {
        this.users = users;
        this.courses = courses;
        this.projects = projects;
        this.members = members;
        this.tasks = tasks;
        this.encoder = encoder;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (users.count() > 0) {
            return;
        }
        LocalDate today = LocalDate.now(clock);
        List<Long> u = seedUsers();
        List<Long> c = seedCourses();
        List<Long> p = seedProjects(today, c, u);
        seedTasks(today, p, u);
    }

    private List<Long> seedUsers() {
        String hash = encoder.encode(DEMO_PASSWORD);
        return List.of(
                        new String[] { "Ana López", "ana.lopez" }, new String[] { "Carlos Rojas", "carlos.rojas" },
                        new String[] { "Lucía Torres", "lucia.torres" }, new String[] { "Diego Fernández", "diego.fernandez" },
                        new String[] { "Mariana Cruz", "mariana.cruz" }, new String[] { "Jorge Silva", "jorge.silva" })
                .stream()
                .map(d -> users.save(new User(d[0], d[1] + "@campus.edu", hash, true)).getId())
                .toList();
    }

    private List<Long> seedCourses() {
        return List.of("Programación Web", "Marketing", "Estadística", "Ingeniería de Software").stream()
                .map(n -> courses.save(new Course(n)).getId()).toList();
    }

    private List<Long> seedProjects(LocalDate t, List<Long> c, List<Long> u) {
        Project p1 = projects.save(new Project("Sistema de gestión de biblioteca", c.get(0),
                "Desarrollar un sistema web para la gestión de la biblioteca de la universidad.",
                t.plusDays(5), ProjectStatus.EN_PROGRESO, "purple"));
        Project p2 = projects.save(new Project("Marketing Digital", c.get(1),
                "Estrategia de redes sociales para un emprendimiento local.",
                t.plusDays(12), ProjectStatus.EN_PROGRESO, "green"));
        Project p3 = projects.save(new Project("Análisis de Datos", c.get(2),
                "Análisis y visualización de datos de encuestas.",
                t.plusDays(20), ProjectStatus.PENDIENTE, "blue"));
        Project p4 = projects.save(new Project("Desarrollo de aplicación móvil", c.get(3),
                "Creación de una app móvil para la gestión de tareas académicas.",
                t.plusDays(45), ProjectStatus.EN_PLANIFICACION, "orange"));

        team(p1, u, 0, new int[] { 0, 1, 4, 5 });
        team(p2, u, 1, new int[] { 0, 1, 3, 4 });
        team(p3, u, 0, new int[] { 0, 1, 2 });
        team(p4, u, 0, new int[] { 0, 1, 2, 3, 4 });
        return List.of(p1.getId(), p2.getId(), p3.getId(), p4.getId());
    }

    /** El primer índice indicado en adminIdx es administrador; el resto son integrantes. */
    private void team(Project project, List<Long> u, int adminIdx, int[] memberIdx) {
        for (int i : memberIdx) {
            members.save(new Member(project.getId(), u.get(i), i == adminIdx ? Role.ADMIN : Role.MIEMBRO));
        }
    }

    private void seedTasks(LocalDate t, List<Long> p, List<Long> u) {
        Long p1 = p.get(0), p2 = p.get(1), p3 = p.get(2);
        List<Task> list = List.of(
                task(t, p1, "Diseñar la interfaz principal", "Crear el diseño de las pantallas principales del sistema.", u.get(0), Priority.ALTA, 2, TaskStatus.EN_PROGRESO, null),
                task(t, p2, "Crear contenido para redes", "Publicar avances y novedades del proyecto.", u.get(1), Priority.MEDIA, 5, TaskStatus.COMPLETADA, -2),
                task(t, p3, "Analizar resultados de encuesta", "Revisar y resumir los datos obtenidos.", u.get(2), Priority.BAJA, 8, TaskStatus.PENDIENTE, null),
                task(t, p2, "Preparar presentación final", "Diseñar diapositivas y revisar contenido.", u.get(3), Priority.ALTA, 12, TaskStatus.PENDIENTE, null),
                task(t, p1, "Revisar avances del proyecto", "Verificar el progreso general y asignar ajustes.", u.get(4), Priority.MEDIA, 10, TaskStatus.EN_PROGRESO, null),
                task(t, p1, "Realizar pruebas de funcionamiento", "Validar la funcionalidad del sistema.", u.get(5), Priority.ALTA, 15, TaskStatus.PENDIENTE, null),
                task(t, p1, "Definir cronograma del proyecto", "Establecer hitos y fechas de entrega.", u.get(0), Priority.MEDIA, -6, TaskStatus.COMPLETADA, -8),
                task(t, p1, "Crear base de datos", "Modelar tablas de libros y usuarios.", u.get(5), Priority.ALTA, -4, TaskStatus.COMPLETADA, -5),
                task(t, p2, "Investigar competencia", "Revisar cuentas similares en redes sociales.", u.get(1), Priority.MEDIA, -5, TaskStatus.COMPLETADA, -12),
                task(t, p3, "Elaborar encuesta", "Redactar las preguntas del cuestionario.", u.get(2), Priority.BAJA, -8, TaskStatus.COMPLETADA, -20),
                task(t, p3, "Recopilar datos", "Aplicar la encuesta a los participantes.", u.get(2), Priority.BAJA, -2, TaskStatus.COMPLETADA, -3),
                task(t, p2, "Diseñar logo", "Crear la identidad visual del emprendimiento.", u.get(3), Priority.MEDIA, -9, TaskStatus.COMPLETADA, -17),
                task(t, p1, "Configurar repositorio", "Crear el repositorio y las ramas de trabajo.", u.get(4), Priority.BAJA, -12, TaskStatus.COMPLETADA, -28),
                task(t, p2, "Calendario de publicaciones", "Planificar las publicaciones del mes.", u.get(0), Priority.MEDIA, 6, TaskStatus.EN_PROGRESO, null),
                task(t, p3, "Limpiar datos de encuesta", "Eliminar respuestas incompletas.", u.get(0), Priority.MEDIA, 1, TaskStatus.EN_PROGRESO, null),
                task(t, p3, "Graficar resultados", "Crear gráficos con los datos limpios.", u.get(0), Priority.BAJA, 9, TaskStatus.EN_PROGRESO, null),
                task(t, p3, "Redactar conclusiones", "Resumir hallazgos principales.", u.get(1), Priority.MEDIA, 13, TaskStatus.EN_PROGRESO, null),
                task(t, p2, "Preparar informe final", "Reunir resultados de la campaña.", u.get(4), Priority.ALTA, -1, TaskStatus.PENDIENTE, null),
                task(t, p1, "Documentar el sistema", "Redactar el manual de uso y la documentación técnica.", null, Priority.MEDIA, 3, TaskStatus.PENDIENTE, null));
        tasks.saveAll(list);
    }

    private static Task task(LocalDate today, Long projectId, String title, String desc, Long assignee,
                             Priority priority, int dueInDays, TaskStatus state, Integer doneInDays) {
        return new Task(projectId, title, desc, assignee, priority, today.plusDays(dueInDays), state,
                doneInDays == null ? null : today.plusDays(doneInDays));
    }
}
