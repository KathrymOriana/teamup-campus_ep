package com.campus.teamup.service;

import java.util.List;

import com.campus.teamup.dto.CourseDtos.CourseResponse;
import com.campus.teamup.repository.CourseRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courses;

    public CourseService(CourseRepository courses) {
        this.courses = courses;
    }

    public List<CourseResponse> list() {
        return courses.findAll().stream().map(c -> new CourseResponse(c.getId(), c.getNombre())).toList();
    }
}
