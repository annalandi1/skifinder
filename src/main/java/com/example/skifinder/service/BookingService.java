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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    public Booking createBooking(UserDetails userDetails, Long equipmentId, LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La data di inizio deve essere prima della data di fine");
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato"));

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attrezzatura non trovata"));

        if (equipment.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Non puoi prenotare la tua stessa attrezzatura");
        }

        List<Booking> conflictingBookings = bookingRepository.findByEquipmentAndStartDateBeforeAndEndDateAfter(
                equipment, endDate, startDate);
        if (!conflictingBookings.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Attrezzatura già prenotata nelle date selezionate");
        }

        Booking booking = new Booking(user, equipment, startDate, endDate);
        booking = bookingRepository.save(booking);

        // Email
        String subject = "Conferma Prenotazione";
        String contentCliente = "Ciao " + user.getName() + ",<br><br>La tua prenotazione per " + equipment.getName() +
                " dal " + startDate + " al " + endDate
                + " è stata confermata.<br><br>Grazie per aver scelto SkiFinder!";
        String contentNoleggiatore = "Ciao " + equipment.getUser().getName() + ",<br><br>La tua attrezzatura "
                + equipment.getName() + " è stata prenotata da " + user.getName() + " dal " + startDate + " al "
                + endDate
                + ".<br><br>Accedi alla tua area per gestire la prenotazione.";

        try {
            emailService.sendEmail(user.getEmail(), subject, contentCliente);
            emailService.sendEmail(equipment.getUser().getEmail(), subject, contentNoleggiatore);
        } catch (IOException e) {
            throw new RuntimeException("Errore nell'invio dell'email di conferma: " + e.getMessage());
        }

        return booking;
    }

    public List<Booking> getUserBookings(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato"));

        // Restituisce tutte le prenotazioni effettuate dall'utente autenticato
        return bookingRepository.findAll()
                .stream()
                .filter(booking -> booking.getUser().getId().equals(user.getId()))
                .toList();
    }

    public List<Booking> getBookingsForOwner(UserDetails userDetails) {
        // ✅ Verifica se l'utente ha ruolo "NOLEGGIATORE"
        boolean isNoleggiatore = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_NOLEGGIATORE"));

        if (!isNoleggiatore) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo i noleggiatori possono accedere alle prenotazioni ricevute.");
        }

        // 🔍 Recupera l'utente noleggiatore
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato"));

        // Restituisce le prenotazioni delle attrezzature che ha caricato
        return bookingRepository.findByEquipment_User(user);
    }

    public void cancelBooking(Long bookingId, UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prenotazione non trovata"));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Non puoi cancellare la prenotazione di un altro utente");
        }

        bookingRepository.delete(booking);
    }

    public List<Booking> getBookingsByEquipment(Long equipmentId) {
        return bookingRepository.findByEquipmentId(equipmentId);
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
