package com.agrorent.api.service;

import com.agrorent.api.dto.BookingRequest;
import com.agrorent.api.model.Booking;
import com.agrorent.api.model.BookingStatus;

import java.util.List;

public interface BookingService {
    Booking createBooking(BookingRequest request, Long farmerId);
    Booking updateBookingStatus(Long bookingId, BookingStatus status, Long userId);
    Booking getBookingById(Long bookingId, Long userId);
    List<Booking> getFarmerBookings(Long farmerId);
    List<Booking> getOwnerBookings(Long ownerId);
    List<Booking> getAllBookings(Long adminId);
    List<Booking> getApprovedBookingsForEquipment(Long equipmentId);
}
