package com.campus.teamup.dto;

import java.util.List;
import java.util.Map;

/** Respuestas del informe de progreso (todo se calcula en el servidor). */
public final class ProgressDtos {

    private ProgressDtos() { }

    public record Stats(int total, int completadas, int enProgreso, int pendientes, int vencidas,
                        int pctCompletadas, int pctEnProgreso, int pctPendientes,
                        Map<String, Integer> prioridad) { }

    public record Health(boolean atencion, List<String> motivos) { }

    public record WeeklySeries(List<String> etiquetas, List<Integer> porcentajes) { }

    public record ProjectProgress(Long id, String nombre, String color, int avance) { }

    public record Team(List<UserResponse> miembros, int activos) { }

    public record ProgressReport(Long projectId, Stats stats, WeeklySeries serie,
                                 List<ProjectProgress> proyectos, Team equipo, Health salud) { }
}
