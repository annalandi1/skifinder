package com.example.skifinder.controller;

import com.example.skifinder.model.Booking;
import com.example.skifinder.model.BookingStatus;
import com.example.skifinder.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    // Crea una prenotazione
    @PostMapping
    public ResponseEntity<Booking> createBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long equipmentId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        Booking booking = bookingService.createBooking(userDetails, equipmentId, startDate, endDate);
        return ResponseEntity.ok(booking);
    }

    // Ottieni tutte le prenotazioni per una specifica attrezzatura
    @GetMapping("/equipment/{equipmentId}")
    public ResponseEntity<List<Booking>> getBookingsByEquipment(@PathVariable Long equipmentId) {
        return ResponseEntity.ok(bookingService.getBookingsByEquipment(equipmentId));
    }

    // Ottieni le prenotazioni dell'utente autenticato
    @GetMapping("/user")
    public ResponseEntity<List<Booking>> getUserBookings(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(bookingService.getUserBookings(userDetails));
    }

    // Ottieni le prenotazioni dell'utente autenticato con role NOLEGGIATORE
    @GetMapping("/rental")
    public ResponseEntity<List<Booking>> getBookingsForOwner(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(bookingService.getBookingsForOwner(userDetails));
    }

    // Cancella una prenotazione (se appartiene all'utente autenticato)
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal UserDetails userDetails) {
        bookingService.cancelBooking(bookingId, userDetails);
        return ResponseEntity.noContent().build();
    }

    // Aggiorna lo stato di una prenotazione
    @PutMapping("/{bookingId}/status")
    public ResponseEntity<Booking> updateStatus(
            @PathVariable Long bookingId,
            @RequestParam BookingStatus status) {
        return ResponseEntity.ok(bookingService.updateBookingStatus(bookingId, status));
    }
}
