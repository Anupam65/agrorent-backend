package com.agrorent.api.service;

import com.agrorent.api.dto.BookingRequest;
import com.agrorent.api.exception.BadRequestException;
import com.agrorent.api.exception.ResourceNotFoundException;
import com.agrorent.api.model.*;
import com.agrorent.api.repository.BookingRepository;
import com.agrorent.api.repository.EquipmentRepository;
import com.agrorent.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Override
    @Transactional
    public Booking createBooking(BookingRequest request, Long farmerId) {
        User farmer = userRepository.findById(farmerId)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer user not found"));

        if (farmer.getRole() != Role.USER && farmer.getRole() != Role.ADMIN) {
            throw new BadRequestException("Only users with the USER role can request equipment bookings");
        }

        Equipment equipment = equipmentRepository.findById(request.getEquipmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found"));

        if (equipment.getAvailabilityStatus() == AvailabilityStatus.MAINTENANCE) {
            throw new BadRequestException("This machinery is currently under maintenance and cannot be rented");
        }

        if (equipment.getOwner().getId().equals(farmerId)) {
            throw new BadRequestException("You cannot rent or book your own listed farm machinery.");
        }

        if (request.getStartDate().isBefore(java.time.LocalDate.now())) {
            throw new BadRequestException("Start date cannot be in the past");
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be on or after start date");
        }

        // Enforce scheduling integrity and prevent clashing bookings
        boolean hasOverlap = bookingRepository.hasOverlappingBookings(
                equipment.getId(), request.getStartDate(), request.getEndDate()
        );

        if (hasOverlap) {
            throw new BadRequestException("The equipment is already booked or requested for the selected dates");
        }

        java.time.LocalDateTime startDateTime = parseDateTime(request.getStartDate(), request.getStartTime());
        java.time.LocalDateTime endDateTime = parseDateTime(request.getEndDate(), request.getEndTime());

        if (startDateTime.isAfter(endDateTime)) {
            throw new BadRequestException("End date/time must be on or after start date/time");
        }

        long diffHours = java.time.Duration.between(startDateTime, endDateTime).toHours();
        long days = diffHours / 24;
        long remainingHours = diffHours % 24;

        if (days == 0 && remainingHours == 0) {
            remainingHours = 1; // Default minimum charge of 1 hour
        }

        BigDecimal dailyPrice = equipment.getPricePerDay();
        BigDecimal hourlyRate = dailyPrice.divide(BigDecimal.valueOf(24), 4, java.math.RoundingMode.HALF_UP);

        BigDecimal daysCost = dailyPrice.multiply(BigDecimal.valueOf(days));
        BigDecimal hoursCost = hourlyRate.multiply(BigDecimal.valueOf(remainingHours));
        BigDecimal totalCost = daysCost.add(hoursCost).setScale(2, java.math.RoundingMode.HALF_UP);

        Booking booking = Booking.builder()
                .farmer(farmer)
                .equipment(equipment)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .totalPrice(totalCost)
                .status(BookingStatus.PENDING)
                .build();

        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public Booking updateBookingStatus(Long bookingId, BookingStatus status, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking transaction not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isOwner = booking.getEquipment().getOwner().getId().equals(userId);
        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isFarmer = booking.getFarmer().getId().equals(userId);

        if (!isOwner && !isAdmin && !isFarmer) {
            throw new BadRequestException("You are not authorized to update this booking");
        }

        // Process status transitions
        if (status == BookingStatus.APPROVED || status == BookingStatus.REJECTED) {
            if (status == BookingStatus.REJECTED && isFarmer) {
                if (booking.getStatus() != BookingStatus.PENDING) {
                    throw new BadRequestException("You can only cancel pending booking requests");
                }
            } else if (!isOwner && !isAdmin) {
                throw new BadRequestException("Only the equipment owner or system administrators can approve or reject booking requests");
            }
            booking.setStatus(status);
            
            // If approved, update active equipment state to RENTED
            if (status == BookingStatus.APPROVED) {
                booking.getEquipment().setAvailabilityStatus(AvailabilityStatus.RENTED);
            }
        } else if (status == BookingStatus.COMPLETED) {
            if (!isOwner && !isAdmin) {
                throw new BadRequestException("Only the equipment owner or system administrators can mark a booking as completed");
            }
            booking.setStatus(status);
            booking.getEquipment().setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        } else if (status == BookingStatus.PENDING) {
            throw new BadRequestException("Cannot revert an active booking status back to PENDING");
        }

        return bookingRepository.save(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public Booking getBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking transaction not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isFarmer = booking.getFarmer().getId().equals(userId);
        boolean isOwner = booking.getEquipment().getOwner().getId().equals(userId);
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isFarmer && !isOwner && !isAdmin) {
            throw new BadRequestException("You are not authorized to view this booking transaction details");
        }

        return booking;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> getFarmerBookings(Long farmerId) {
        return bookingRepository.findByFarmerId(farmerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> getOwnerBookings(Long ownerId) {
        return bookingRepository.findByOwnerId(ownerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> getAllBookings(Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (admin.getRole() != Role.ADMIN) {
            throw new BadRequestException("Only system administrators can access full booking history logs");
        }

        return bookingRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> getApprovedBookingsForEquipment(Long equipmentId) {
        return bookingRepository.findApprovedBookingsByEquipmentId(equipmentId);
    }

    private java.time.LocalDateTime parseDateTime(java.time.LocalDate date, String timeStr) {
        if (date == null) return null;
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return date.atStartOfDay();
        }
        try {
            String time = timeStr.trim();
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(\\d+):(\\d+)\\s*(AM|PM)", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(time);
            if (!matcher.matches()) {
                return date.atStartOfDay();
            }
            int hour = Integer.parseInt(matcher.group(1));
            int minute = Integer.parseInt(matcher.group(2));
            String ampm = matcher.group(3).toUpperCase();
            
            if ("PM".equals(ampm) && hour < 12) {
                hour += 12;
            } else if ("AM".equals(ampm) && hour == 12) {
                hour = 0;
            }
            return date.atTime(hour, minute);
        } catch (Exception e) {
            return date.atStartOfDay();
        }
    }
}
