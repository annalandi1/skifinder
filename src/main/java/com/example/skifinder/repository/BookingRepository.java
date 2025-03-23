package com.example.skifinder.repository;

import com.example.skifinder.model.Booking;
import com.example.skifinder.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Trova prenotazioni che si sovrappongono con il periodo richiesto
    List<Booking> findByEquipmentAndStartDateBeforeAndEndDateAfter(
            Equipment equipment, LocalDate endDate, LocalDate startDate);
}
