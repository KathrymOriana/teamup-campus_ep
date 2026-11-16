package com.campus.teamup.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Role {
    ADMIN("Administrador"), MIEMBRO("Integrante");

    private final String label;

    Role(String label) { this.label = label; }

    @JsonValue
    public String label() { return label; }
}
