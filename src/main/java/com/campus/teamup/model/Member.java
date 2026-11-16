package com.campus.teamup.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** Pertenencia de un usuario a un proyecto, con su rol. */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "projectId", "userId" }))
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role rol;

    protected Member() { }

    public Member(Long projectId, Long userId, Role rol) {
        this.projectId = projectId;
        this.userId = userId;
        this.rol = rol;
    }

    public void setRol(Role rol) { this.rol = rol; }

    public Long getId() { return id; }
    public Long getProjectId() { return projectId; }
    public Long getUserId() { return userId; }
    public Role getRol() { return rol; }
    public boolean isAdmin() { return rol == Role.ADMIN; }
}
