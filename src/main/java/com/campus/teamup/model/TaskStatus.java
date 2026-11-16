package com.campus.teamup.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TaskStatus {
    PENDIENTE("Pendiente"), EN_PROGRESO("En progreso"), COMPLETADA("Completada");

    private final String label;

    TaskStatus(String label) { this.label = label; }

    @JsonValue
    public String label() { return label; }
}
