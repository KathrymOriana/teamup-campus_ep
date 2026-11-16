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
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(length = 500)
    private String descripcion;

    /** Nulo = sin asignar. */
    private Long responsableId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Priority prioridad;

    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TaskStatus estado;

    private LocalDate completadaEn;

    protected Task() { }

    public Task(Long projectId, String titulo, String descripcion, Long responsableId,
                Priority prioridad, LocalDate fecha, TaskStatus estado, LocalDate completadaEn) {
        this.projectId = projectId;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.responsableId = responsableId;
        this.prioridad = prioridad;
        this.fecha = fecha;
        this.estado = estado;
        this.completadaEn = completadaEn;
    }

    /* ---------- comportamiento del dominio ---------- */

    public boolean isCompleted() { return estado == TaskStatus.COMPLETADA; }

    public boolean isOverdue(LocalDate today) { return !isCompleted() && fecha.isBefore(today); }

    /** Pendiente de completar y con fecha límite dentro de los próximos 7 días (incluye hoy). */
    public boolean isDueSoon(LocalDate today) {
        return !isCompleted() && !fecha.isBefore(today) && !fecha.isAfter(today.plusDays(7));
    }

    public void changeState(TaskStatus newState, LocalDate today) {
        this.estado = newState;
        this.completadaEn = newState == TaskStatus.COMPLETADA ? today : null;
    }

    public void edit(String titulo, String descripcion, Long responsableId,
                     Priority prioridad, LocalDate fecha, TaskStatus newState, LocalDate today) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.responsableId = responsableId;
        this.prioridad = prioridad;
        this.fecha = fecha;
        if (newState != this.estado) {
            changeState(newState, today);
        }
    }

    public Long getId() { return id; }
    public Long getProjectId() { return projectId; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public Long getResponsableId() { return responsableId; }
    public Priority getPrioridad() { return prioridad; }
    public LocalDate getFecha() { return fecha; }
    public TaskStatus getEstado() { return estado; }
    public LocalDate getCompletadaEn() { return completadaEn; }
}
