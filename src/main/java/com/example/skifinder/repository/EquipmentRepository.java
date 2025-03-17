package com.example.skifinder.repository;

import com.example.skifinder.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    @Query("SELECT e FROM Equipment e JOIN e.bookings b WHERE b.user.id = :userId")
    List<Equipment> findByBookingsUserId(@Param("userId") Long userId);
}
