package com.example.skifinder.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@Entity
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // Nome dell'attrezzatura
    private String description; // Descrizione dell'attrezzatura
    private double price; // Prezzo di noleggio
    private String size; // Taglia (cm, EU, XS-S-M-L-XL, vuoto se non necessario)
    private String type;
    private String macroCategory; // Macro categoria per gestione automatica delle taglie

    @Column(nullable = false)
    private boolean isAvailable = true; // disponibilità

    private List<String> imagePaths; // Nuovo campo per il percorso dell'immagine

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonIgnoreProperties("equipments")
    private User user; // Utente proprietario dell'attrezzatura

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "location_id", referencedColumnName = "id")
    private Location location; // Posizione geografica dell'attrezzatura

    @OneToMany(mappedBy = "equipment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings; // Prenotazioni associate all'attrezzatura
}
