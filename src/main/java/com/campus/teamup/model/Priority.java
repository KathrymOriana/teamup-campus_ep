package com.campus.teamup.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Priority {
    ALTA("Alta"), MEDIA("Media"), BAJA("Baja");

    private final String label;

    Priority(String label) { this.label = label; }

    @JsonValue
    public String label() { return label; }
}
