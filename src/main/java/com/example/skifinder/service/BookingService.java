package com.example.skifinder.service;

import com.example.skifinder.model.Booking;
import com.example.skifinder.model.BookingStatus;
import com.example.skifinder.model.Equipment;
import com.example.skifinder.model.User;
import com.example.skifinder.repository.BookingRepository;
import com.example.skifinder.repository.EquipmentRepository;
import com.example.skifinder.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    // Effettua una prenotazione
    public Booking createBooking(Long userId, Long equipmentId, LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La data di inizio deve essere prima della data di fine");
        }

        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Equipment> equipmentOpt = equipmentRepository.findById(equipmentId);

        if (userOpt.isEmpty() || equipmentOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente o attrezzatura non trovata");
        }

        User user = userOpt.get();
        Equipment equipment = equipmentOpt.get();

        if (equipment.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Non puoi prenotare la tua stessa attrezzatura");
        }

        // Controlliamo se l'attrezzatura è disponibile nel periodo scelto
        List<Booking> conflictingBookings = bookingRepository.findByEquipmentAndStartDateBeforeAndEndDateAfter(
                equipment, endDate, startDate);
        if (!conflictingBookings.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Attrezzatura già prenotata nelle date selezionate");
        }

        Booking booking = new Booking(user, equipment, startDate, endDate);
        booking = bookingRepository.save(booking);

        // Invia email di conferma al cliente e al noleggiatore
        String subject = "Conferma Prenotazione";
        String contentCliente = "Ciao " + user.getName() + ",<br><br>La tua prenotazione per " + equipment.getName() +
                " dal " + startDate + " al " + endDate
                + " è stata confermata.<br><br>Grazie per aver scelto SkiFinder!";

        String contentNoleggiatore = "Ciao " + equipment.getUser().getName() + ",<br><br>La tua attrezzatura "
                + equipment.getName() +
                " è stata prenotata da " + user.getName() + " dal " + startDate + " al " + endDate
                + ".<br><br>Accedi alla tua area per gestire la prenotazione.";

        try {
            emailService.sendEmail(user.getEmail(), subject, contentCliente);
            emailService.sendEmail(equipment.getUser().getEmail(), subject, contentNoleggiatore);
        } catch (IOException e) {
            throw new RuntimeException("Errore nell'invio dell'email di conferma: " + e.getMessage());
        }

        return booking;
    }

    // Ottiene tutte le prenotazioni di un utente
    public List<Booking> getUserBookings(Long userId) {
        return bookingRepository.findAll()
                .stream()
                .filter(booking -> booking.getUser().getId().equals(userId))
                .toList();
    }

    // Cancella una prenotazione
    public void cancelBooking(Long bookingId, Long userId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Prenotazione non trovata");
        }

        Booking booking = bookingOpt.get();
        if (!booking.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Non puoi cancellare la prenotazione di un altro utente");
        }

        bookingRepository.deleteById(bookingId);
    }

    public Booking updateBookingStatus(Long bookingId, BookingStatus status) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Prenotazione non trovata");
        }

        Booking booking = bookingOpt.get();
        booking.setStatus(status);
        return bookingRepository.save(booking);
    }

}
