package com.example.skifinder.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Preference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;

    // Costruttore vuoto richiesto da JPA
    public Preference() {
    }

    // Costruttore personalizzato
    public Preference(User user, Equipment equipment) {
        this.user = user;
        this.equipment = equipment;
    }
}
