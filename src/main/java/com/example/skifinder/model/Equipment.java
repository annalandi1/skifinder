package com.example.skifinder.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // Nome dell'attrezzatura
    private String description; // Descrizione dell'attrezzatura
    private double price; // Prezzo di noleggio
    private String size; // Taglia (es. "42", "170cm")
    private String type; // Tipo di attrezzatura (es. "Sci", "Scarponi")
    private boolean isAvailable = true; // disponibilità

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user; // Utente proprietario dell'attrezzatura (opzionale)

    @ManyToOne
    @JoinColumn(name = "location_id", referencedColumnName = "id")
    private Location location; // Posizione geografica dell'attrezzatura

    @OneToMany(mappedBy = "equipment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings; // Prenotazioni associate all'attrezzatura
}