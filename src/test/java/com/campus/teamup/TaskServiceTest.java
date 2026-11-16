package com.campus.teamup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import com.campus.teamup.dto.TaskDtos.TaskRequest;
import com.campus.teamup.exception.ApiException;
import com.campus.teamup.model.Member;
import com.campus.teamup.model.Priority;
import com.campus.teamup.model.Role;
import com.campus.teamup.model.Task;
import com.campus.teamup.model.TaskStatus;
import com.campus.teamup.repository.TaskRepository;
import com.campus.teamup.service.MembershipService;
import com.campus.teamup.service.TaskService;

import org.junit.jupiter.api.Test;

/** Reglas de permisos de la capa de servicio, probadas con dependencias simuladas. */
class TaskServiceTest {

    private final TaskRepository repo = mock(TaskRepository.class);
    private final MembershipService membership = mock(MembershipService.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-06T12:00:00Z"), ZoneOffset.UTC);
    private final TaskService service = new TaskService(repo, membership, clock);

    private Task taskOf(Long assignee) {
        return new Task(1L, "Tarea", "d", assignee, Priority.MEDIA, LocalDate.of(2026, 11, 10), TaskStatus.PENDIENTE, null);
    }

    @Test
    void integranteAjenoNoPuedeCambiarElEstado() {
        when(repo.findById(5L)).thenReturn(Optional.of(taskOf(10L)));
        when(membership.requireMember(1L, 99L)).thenReturn(new Member(1L, 99L, Role.MIEMBRO));

        ApiException error = assertThrows(ApiException.class,
                () -> service.changeState(5L, TaskStatus.COMPLETADA, 99L));
        assertEquals(403, error.getStatus().value());
    }

    @Test
    void responsableCompletaSuTareaYQuedaLaFecha() {
        when(repo.findById(5L)).thenReturn(Optional.of(taskOf(10L)));
        when(membership.requireMember(1L, 10L)).thenReturn(new Member(1L, 10L, Role.MIEMBRO));

        var response = service.changeState(5L, TaskStatus.COMPLETADA, 10L);

        assertEquals(TaskStatus.COMPLETADA, response.estado());
        assertNotNull(response.completadaEn());
    }

    @Test
    void integranteNoPuedeAsignarTareasAOtraPersona() {
        when(membership.requireMember(1L, 10L)).thenReturn(new Member(1L, 10L, Role.MIEMBRO));
        TaskRequest request = new TaskRequest(1L, "Nueva", null, 20L, Priority.ALTA,
                LocalDate.of(2026, 11, 20), TaskStatus.PENDIENTE);

        ApiException error = assertThrows(ApiException.class, () -> service.create(10L, request));

        assertEquals(403, error.getStatus().value());
        verify(repo, never()).save(any());
    }
}
