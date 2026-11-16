package com.campus.teamup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import com.campus.teamup.dto.ProgressDtos.Health;
import com.campus.teamup.dto.ProgressDtos.Stats;
import com.campus.teamup.model.Priority;
import com.campus.teamup.model.Task;
import com.campus.teamup.model.TaskStatus;
import com.campus.teamup.repository.ProjectRepository;
import com.campus.teamup.repository.TaskRepository;
import com.campus.teamup.repository.UserRepository;
import com.campus.teamup.service.MembershipService;
import com.campus.teamup.service.ProgressService;

import org.junit.jupiter.api.Test;

/** Prueba de la regla de negocio de indicadores con una fecha fija (no depende de la base de datos). */
class ProgressServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 6);
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-06T12:00:00Z"), ZoneOffset.UTC);
    private final ProgressService service = new ProgressService(
            mock(TaskRepository.class), mock(ProjectRepository.class), mock(MembershipService.class),
            mock(UserRepository.class), clock);

    private Task task(TaskStatus status, int dueInDays, Long assignee) {
        return new Task(1L, "t", "d", assignee, Priority.MEDIA, TODAY.plusDays(dueInDays), status,
                status == TaskStatus.COMPLETADA ? TODAY : null);
    }

    @Test
    void calculaPorcentajesYVencidas() {
        Stats stats = service.stats(List.of(
                task(TaskStatus.COMPLETADA, -3, 1L),
                task(TaskStatus.EN_PROGRESO, 2, 1L),
                task(TaskStatus.PENDIENTE, -1, 1L),
                task(TaskStatus.PENDIENTE, 5, 1L)));

        assertEquals(4, stats.total());
        assertEquals(25, stats.pctCompletadas());
        assertEquals(1, stats.vencidas()); // la completada no cuenta aunque su fecha pasó
    }

    @Test
    void sinTareasElAvanceEsCero() {
        assertEquals(0, service.stats(List.of()).pctCompletadas());
    }

    @Test
    void saludAvisaTareasVencidasYSinResponsable() {
        Health health = service.health(List.of(), List.of(
                task(TaskStatus.PENDIENTE, -2, 1L),
                task(TaskStatus.PENDIENTE, 4, null)));

        assertTrue(health.atencion());
        assertEquals(2, health.motivos().size());
    }

    @Test
    void proyectoSanoNoNecesitaAtencion() {
        Health health = service.health(List.of(), List.of(task(TaskStatus.EN_PROGRESO, 6, 1L)));
        assertFalse(health.atencion());
    }
}
