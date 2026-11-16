package com.campus.teamup.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ProjectStatus {
    EN_PROGRESO("En progreso"), PENDIENTE("Pendiente"), EN_PLANIFICACION("En planificación");

    private final String label;

    ProjectStatus(String label) { this.label = label; }

    /** El JSON usa la etiqueta legible, igual que la interfaz. */
    @JsonValue
    public String label() { return label; }
}
