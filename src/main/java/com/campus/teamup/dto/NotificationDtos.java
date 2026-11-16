package com.campus.teamup.dto;

public final class NotificationDtos {

    private NotificationDtos() { }

    public record Notification(String nivel, String icono, String texto, Long projectId) { }
}
