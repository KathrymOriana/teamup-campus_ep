package com.campus.teamup.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, unique = true, length = 160)
    private String email;

    /** Nulo cuando un administrador invitó a la persona y esta todavía no creó su cuenta. */
    @Column(length = 100)
    private String passwordHash;

    /** true = existe una foto de perfil en assets/img (solo las cuentas de ejemplo). */
    private boolean foto;

    protected User() { }

    public User(String nombre, String email, String passwordHash, boolean foto) {
        this.nombre = nombre;
        this.email = email;
        this.passwordHash = passwordHash;
        this.foto = foto;
    }

    public boolean hasPassword() { return passwordHash != null; }

    /** Una persona invitada reclama su cuenta al registrarse. */
    public void claim(String nombre, String passwordHash) {
        this.nombre = nombre;
        this.passwordHash = passwordHash;
    }

    public void changePassword(String passwordHash) { this.passwordHash = passwordHash; }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public boolean isFoto() { return foto; }
}
