package com.campus.teamup.dto;

public final class CourseDtos {

    private CourseDtos() { }

    public record CourseResponse(Long id, String nombre) { }
}
