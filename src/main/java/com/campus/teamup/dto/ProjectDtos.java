package com.campus.teamup.dto;

import java.time.LocalDate;
import java.util.List;

import com.campus.teamup.model.ProjectStatus;
import com.campus.teamup.model.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class ProjectDtos {

    private ProjectDtos() { }

    public record ProjectRequest(
            @NotBlank(message = "Escribe el nombre del proyecto.") @Size(max = 120, message = "Máximo 120 caracteres.") String nombre,
            @NotNull(message = "Elige un curso.") Long cursoId,
            @Size(max = 500, message = "Máximo 500 caracteres.") String descripcion,
            @NotNull(message = "Indica la fecha de entrega.") LocalDate entrega,
            @NotNull(message = "Elige un estado.") ProjectStatus estado) { }

    public record ProjectResponse(
            Long id, String nombre, Long cursoId, String curso, String descripcion, LocalDate entrega,
            ProjectStatus estado, String color, Role rol, List<UserResponse> miembros,
            int totalTareas, int tareasHechas, int avance,
            boolean atrasado, boolean necesitaAtencion, List<String> motivos) { }

    public record ProjectSummary(int total, int activos, int enProgreso, int pendientes, int atrasados) { }

    public record AddMemberRequest(
            String nombre,
            @NotBlank(message = "Ingresa el correo.") @Email(message = "Ingresa un correo válido.") String email,
            @NotNull(message = "Elige un rol.") Role rol) { }

    public record RoleRequest(@NotNull(message = "Elige un rol.") Role rol) { }

    public record MemberResponse(Long userId, String nombre, String email, boolean foto, Role rol) { }
}
