package com.example.skifinder.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Se vuoi tenere lo username come campo separato, puoi lasciarlo unique, ma non
    // obbligatorio
    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String email; // Per login tramite email

    @Column(unique = true)
    private String phoneNumber; // Per login tramite telefono

    @Column(nullable = false)
    private String password;

    // Dati opzionali del profilo
    private String name; // Nome
    private String surname; // Cognome
    private String photo; // URL / path a foto profilo

    @Enumerated(EnumType.STRING)
    private Role role;
}
