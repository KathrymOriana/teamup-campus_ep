package com.campus.teamup.controller;

import java.util.List;

import com.campus.teamup.dto.CourseDtos.CourseResponse;
import com.campus.teamup.service.CourseService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService service;

    public CourseController(CourseService service) {
        this.service = service;
    }

    @GetMapping
    public List<CourseResponse> list() {
        return service.list();
    }
}
