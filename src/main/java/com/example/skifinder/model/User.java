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

    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String email; // Per login tramite email

    @Column(name = "phonenumber", unique = true)
    private String phoneNumber; // Per login tramite telefono

    @Column(nullable = false)
    private String password;

    // Dati opzionali del profilo
    private String name;
    private String surname;

    @Column(name = "photo")
    private String photo; // URL a foto profilo

    @Enumerated(EnumType.STRING)
    private Role role;
}
