package com.campus.teamup.dto;

import java.time.LocalDate;

import com.campus.teamup.model.Priority;
import com.campus.teamup.model.Task;
import com.campus.teamup.model.TaskStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class TaskDtos {

    private TaskDtos() { }

    public record TaskRequest(
            @NotNull(message = "Elige un proyecto.") Long projectId,
            @NotBlank(message = "Escribe el título de la tarea.") @Size(max = 150, message = "Máximo 150 caracteres.") String titulo,
            @Size(max = 500, message = "Máximo 500 caracteres.") String descripcion,
            Long responsableId,
            @NotNull(message = "Elige una prioridad.") Priority prioridad,
            @NotNull(message = "Indica la fecha límite.") LocalDate fecha,
            @NotNull(message = "Elige un estado.") TaskStatus estado) { }

    public record StateRequest(@NotNull(message = "Elige un estado.") TaskStatus estado) { }

    /** `editable` y `vencida` los decide el servidor: la interfaz solo los muestra. */
    public record TaskResponse(
            Long id, Long projectId, String titulo, String descripcion, Long responsableId,
            Priority prioridad, LocalDate fecha, TaskStatus estado, LocalDate completadaEn,
            boolean vencida, boolean proxima, boolean editable) {

        public static TaskResponse from(Task t, LocalDate today, boolean editable) {
            return new TaskResponse(t.getId(), t.getProjectId(), t.getTitulo(), t.getDescripcion(),
                    t.getResponsableId(), t.getPrioridad(), t.getFecha(), t.getEstado(), t.getCompletadaEn(),
                    t.isOverdue(today), t.isDueSoon(today), editable);
        }
    }
}
