package com.campus.teamup.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false)
    private Long cursoId;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    private LocalDate entrega;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectStatus estado;

    /** Color de identificación en la interfaz: purple | green | blue | orange. */
    @Column(nullable = false, length = 10)
    private String color;

    protected Project() { }

    public Project(String nombre, Long cursoId, String descripcion, LocalDate entrega, ProjectStatus estado, String color) {
        this.nombre = nombre;
        this.cursoId = cursoId;
        this.descripcion = descripcion;
        this.entrega = entrega;
        this.estado = estado;
        this.color = color;
    }

    public void update(String nombre, Long cursoId, String descripcion, LocalDate entrega, ProjectStatus estado) {
        this.nombre = nombre;
        this.cursoId = cursoId;
        this.descripcion = descripcion;
        this.entrega = entrega;
        this.estado = estado;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public Long getCursoId() { return cursoId; }
    public String getDescripcion() { return descripcion; }
    public LocalDate getEntrega() { return entrega; }
    public ProjectStatus getEstado() { return estado; }
    public String getColor() { return color; }
}
