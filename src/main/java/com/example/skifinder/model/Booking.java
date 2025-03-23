package com.example.skifinder.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate startDate; // Data di inizio noleggio

    @Column(nullable = false)
    private LocalDate endDate; // Data di fine noleggio

    @ManyToOne
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment; // Attrezzatura noleggiata

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Utente che effettua il noleggio

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING; // Stato della prenotazione

    private boolean paid = false;

    public Booking() {
    }

    public Booking(User user, Equipment equipment, LocalDate startDate, LocalDate endDate) {
        this.user = user;
        this.equipment = equipment;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = BookingStatus.PENDING;
    }
}
