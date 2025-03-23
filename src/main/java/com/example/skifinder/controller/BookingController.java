package com.example.skifinder.controller;

import com.example.skifinder.model.Booking;
import com.example.skifinder.model.BookingStatus;
import com.example.skifinder.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Booking createBooking(
            @RequestParam Long userId,
            @RequestParam Long equipmentId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return bookingService.createBooking(
                userId,
                equipmentId,
                LocalDate.parse(startDate),
                LocalDate.parse(endDate));
    }

    @GetMapping("/user/{userId}")
    public List<Booking> getUserBookings(@PathVariable Long userId) {
        return bookingService.getUserBookings(userId);
    }

    @PutMapping("/{bookingId}/confirm")
    public Booking confirmBooking(@PathVariable Long bookingId) {
        return bookingService.updateBookingStatus(bookingId, BookingStatus.CONFIRMED);
    }

    @PutMapping("/{bookingId}/complete")
    public Booking completeBooking(@PathVariable Long bookingId) {
        return bookingService.updateBookingStatus(bookingId, BookingStatus.COMPLETED);
    }

    @DeleteMapping("/{bookingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelBooking(@PathVariable Long bookingId, @RequestParam Long userId) {
        bookingService.cancelBooking(bookingId, userId);
    }
}
