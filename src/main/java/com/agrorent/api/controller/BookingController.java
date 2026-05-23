package com.agrorent.api.controller;

import com.agrorent.api.dto.BookingRequest;
import com.agrorent.api.model.Booking;
import com.agrorent.api.model.BookingStatus;
import com.agrorent.api.security.UserPrincipal;
import com.agrorent.api.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Booking> createBooking(
            @Valid @RequestBody BookingRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        Booking created = bookingService.createBooking(request, currentUser.getId());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Booking> updateBookingStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        String statusStr = payload.get("status");
        if (statusStr == null) {
            return ResponseEntity.badRequest().build();
        }
        
        BookingStatus status = BookingStatus.valueOf(statusStr.toUpperCase());
        Booking updated = bookingService.updateBookingStatus(id, status, currentUser.getId());
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/my-rentals")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<Booking>> getMyRentals(@AuthenticationPrincipal UserPrincipal currentUser) {
        List<Booking> rentals = bookingService.getFarmerBookings(currentUser.getId());
        return ResponseEntity.ok(rentals);
    }

    @GetMapping("/owner-requests")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<Booking>> getOwnerRequests(@AuthenticationPrincipal UserPrincipal currentUser) {
        List<Booking> requests = bookingService.getOwnerBookings(currentUser.getId());
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        return ResponseEntity.ok(bookingService.getBookingById(id, currentUser.getId()));
    }

    @GetMapping("/equipment/{equipmentId}/approved")
    public ResponseEntity<List<Booking>> getApprovedBookingsForEquipment(@PathVariable Long equipmentId) {
        List<Booking> bookings = bookingService.getApprovedBookingsForEquipment(equipmentId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Booking>> getAllBookings(@AuthenticationPrincipal UserPrincipal currentUser) {
        List<Booking> bookings = bookingService.getAllBookings(currentUser.getId());
        return ResponseEntity.ok(bookings);
    }
}
