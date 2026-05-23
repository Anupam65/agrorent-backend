package com.agrorent.api.repository;

import com.agrorent.api.model.Booking;
import com.agrorent.api.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    @Query("SELECT b FROM Booking b WHERE b.farmer.id = :farmerId")
    List<Booking> findByFarmerId(@Param("farmerId") Long farmerId);

    @Query("SELECT b FROM Booking b WHERE b.equipment.owner.id = :ownerId")
    List<Booking> findByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT b FROM Booking b WHERE b.status = :status")
    List<Booking> findByStatus(@Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.equipment.id = :equipmentId AND b.status = com.agrorent.api.model.BookingStatus.APPROVED")
    List<Booking> findApprovedBookingsByEquipmentId(@Param("equipmentId") Long equipmentId);

    // Dynamic verification query checking date overlaps for APPROVED or PENDING bookings
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.equipment.id = :equipmentId " +
            "AND b.status IN (com.agrorent.api.model.BookingStatus.APPROVED, com.agrorent.api.model.BookingStatus.PENDING) " +
            "AND NOT (b.endDate < :startDate OR b.startDate > :endDate)")
    boolean hasOverlappingBookings(
            @Param("equipmentId") Long equipmentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
