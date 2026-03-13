package com.project.render.Service;

import com.project.render.DTO.BarberConflictResponse;
import com.project.render.DTO.BarberCreateRequest;
import com.project.render.DTO.BarberTemporaryInactiveRequest;
import com.project.render.DTO.BarberUpdateRequest;
import com.project.render.Entity.Barber;
import com.project.render.Entity.Booking;
import com.project.render.Entity.Salon;
import com.project.render.Exception.BookingConflictException;
import com.project.render.Repository.BarberRepository;
import com.project.render.Repository.BookingRepository;
import com.project.render.Repository.SalonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BarberService {

    @Autowired
    private BarberRepository barberRepository;

    @Autowired
    private SalonRepository salonRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private NotificationService notificationService;

    public Barber addBarber(String salonId, BarberCreateRequest request) {
        Salon salon = salonRepository.findById(salonId)
                .orElseThrow(() -> new RuntimeException("Salon not found"));

        validateName(request.getName());
        validateWorkingHours(request.getWorkingStartTime(), request.getWorkingEndTime(), request.getLunchStart(), request.getLunchEnd());
        validateShiftInsideSalon(request.getWorkingStartTime(), request.getWorkingEndTime(), salon.getOpentime(), salon.getClosetime());

        Barber barber = Barber.builder()
                .salonId(salonId)
                .name(request.getName().trim())
                .active(request.getActive() == null || request.getActive())
                .workingStartTime(request.getWorkingStartTime())
                .workingEndTime(request.getWorkingEndTime())
                .lunchStart(request.getLunchStart())
                .lunchEnd(request.getLunchEnd())
                .leaves(normalizeLeaves(request.getLeaves()))
                .weeklyOffDays(normalizeWeeklyOffDays(request.getWeeklyOffDays()))
                .build();

        Barber savedBarber = barberRepository.save(barber);

        if (salon.getBarbersIds() == null) {
            salon.setBarbersIds(new ArrayList<>());
        }

        if (!salon.getBarbersIds().contains(savedBarber.getId())) {
            salon.getBarbersIds().add(savedBarber.getId());
        }

        salonRepository.save(salon);
        return savedBarber;
    }

    public List<Barber> getBarbersBySalon(String salonId) {
        return barberRepository.findBySalonId(salonId);
    }

    public Barber updateLeaves(String barberId, List<LocalDate> leaves, Boolean autoCancelConflictingBookings, String cancellationReason) {
        Barber barber = barberRepository.findById(barberId)
                .orElseThrow(() -> new RuntimeException("Barber not found"));

        List<LocalDate> normalizedLeaves = normalizeLeaves(leaves);

        List<Booking> conflicts = bookingRepository.findByBarberIdAndBookingDateInAndStatus(
                barberId,
                normalizedLeaves.stream().filter(d -> !d.isBefore(LocalDate.now())).toList(),
                "CONFIRMED"
        );

        handleConflictsIfNeeded(conflicts, autoCancelConflictingBookings, cancellationReason, "barber leave");

        barber.setLeaves(normalizedLeaves);
        return barberRepository.save(barber);
    }

    public Barber updateWeeklyOff(String barberId, Set<DayOfWeek> weeklyOffDays, Boolean autoCancelConflictingBookings, String cancellationReason) {
        Barber barber = barberRepository.findById(barberId)
                .orElseThrow(() -> new RuntimeException("Barber not found"));

        Set<DayOfWeek> normalizedDays = normalizeWeeklyOffDays(weeklyOffDays);

        List<Booking> futureConfirmed = bookingRepository.findByBarberIdAndStatus(barberId, "CONFIRMED")
                .stream()
                .filter(b -> !b.getBookingDate().isBefore(LocalDate.now()))
                .filter(b -> normalizedDays.contains(b.getBookingDate().getDayOfWeek()))
                .toList();

        handleConflictsIfNeeded(futureConfirmed, autoCancelConflictingBookings, cancellationReason, "weekly off update");

        barber.setWeeklyOffDays(normalizedDays);
        return barberRepository.save(barber);
    }

    public Barber updateBarber(String barberId, BarberUpdateRequest request) {
        Barber barber = barberRepository.findById(barberId)
                .orElseThrow(() -> new RuntimeException("Barber not found"));

        Salon salon = salonRepository.findById(barber.getSalonId())
                .orElseThrow(() -> new RuntimeException("Salon not found"));

        LocalTime workingStart = request.getWorkingStartTime() != null ? request.getWorkingStartTime() : barber.getWorkingStartTime();
        LocalTime workingEnd = request.getWorkingEndTime() != null ? request.getWorkingEndTime() : barber.getWorkingEndTime();
        LocalTime lunchStart = request.getLunchStart() != null ? request.getLunchStart() : barber.getLunchStart();
        LocalTime lunchEnd = request.getLunchEnd() != null ? request.getLunchEnd() : barber.getLunchEnd();

        validateWorkingHours(workingStart, workingEnd, lunchStart, lunchEnd);
        validateShiftInsideSalon(workingStart, workingEnd, salon.getOpentime(), salon.getClosetime());

        List<Booking> futureConfirmed = bookingRepository.findByBarberIdAndStatus(barberId, "CONFIRMED")
                .stream()
                .filter(b -> !b.getBookingDate().isBefore(LocalDate.now()))
                .filter(b -> bookingFallsOutsideShift(b, workingStart, workingEnd, lunchStart, lunchEnd))
                .toList();

        handleConflictsIfNeeded(futureConfirmed, request.getAutoCancelConflictingBookings(), request.getCancellationReason(), "barber schedule update");

        if (request.getActive() != null) {
            barber.setActive(request.getActive());
        }

        barber.setWorkingStartTime(workingStart);
        barber.setWorkingEndTime(workingEnd);
        barber.setLunchStart(lunchStart);
        barber.setLunchEnd(lunchEnd);

        if (request.getLeaves() != null) {
            barber.setLeaves(normalizeLeaves(request.getLeaves()));
        }

        if (request.getWeeklyOffDays() != null) {
            barber.setWeeklyOffDays(normalizeWeeklyOffDays(request.getWeeklyOffDays()));
        }

        return barberRepository.save(barber);
    }

    public Barber markTemporaryInactive(String barberId, BarberTemporaryInactiveRequest request) {
        Barber barber = barberRepository.findById(barberId)
                .orElseThrow(() -> new RuntimeException("Barber not found"));

        if (request.getDate() == null || request.getStartTime() == null || request.getEndTime() == null) {
            throw new IllegalArgumentException("Date, start time and end time are required");
        }

        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new IllegalArgumentException("Inactive start time must be before inactive end time");
        }

        if (request.getStartTime().isBefore(barber.getWorkingStartTime()) || request.getEndTime().isAfter(barber.getWorkingEndTime())) {
            throw new IllegalArgumentException("Inactive time must be inside barber working hours");
        }

        List<Booking> conflicts = bookingRepository.findByBarberIdAndBookingDateAndStatus(barberId, request.getDate(), "CONFIRMED")
                .stream()
                .filter(b -> request.getStartTime().isBefore(b.getEndTime()) && request.getEndTime().isAfter(b.getStartTime()))
                .toList();

        handleConflictsIfNeeded(
                conflicts,
                request.getAutoCancelConflictingBookings(),
                request.getReason(),
                "temporary inactivity"
        );

        TemporaryInactiveSlot slot = TemporaryInactiveSlot.builder()
                .date(request.getDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .reason(request.getReason())
                .build();

        if (barber.getTemporaryInactiveSlots() == null) {
            barber.setTemporaryInactiveSlots(new ArrayList<>());
        }

        barber.getTemporaryInactiveSlots().add(slot);

        return barberRepository.save(barber);
    }

    public String deleteBarber(String barberId) {
        Barber barber = barberRepository.findById(barberId)
                .orElseThrow(() -> new RuntimeException("Barber not found"));

        List<Booking> futureConfirmed = bookingRepository.findByBarberIdAndStatus(barberId, "CONFIRMED")
                .stream()
                .filter(b -> !b.getBookingDate().isBefore(LocalDate.now()))
                .toList();

        if (!futureConfirmed.isEmpty()) {
            throw new IllegalArgumentException("Cannot delete barber with active future bookings");
        }

        Salon salon = salonRepository.findById(barber.getSalonId())
                .orElseThrow(() -> new RuntimeException("Salon not found"));

        if (salon.getBarbersIds() != null) {
            salon.getBarbersIds().remove(barberId);
            salonRepository.save(salon);
        }

        barberRepository.delete(barber);
        return "barber deleted successfully";
    }

    private void validateName(String name) {
        if (name == null || name.trim().isBlank()) {
            throw new IllegalArgumentException("Barber name is required");
        }
    }

    private void validateWorkingHours(LocalTime workingStart, LocalTime workingEnd, LocalTime lunchStart, LocalTime lunchEnd) {
        if (workingStart == null || workingEnd == null) {
            throw new IllegalArgumentException("Working start time and end time are required");
        }

        if (!workingStart.isBefore(workingEnd)) {
            throw new IllegalArgumentException("Working start time must be before end time");
        }

        boolean lunchBothNull = lunchStart == null && lunchEnd == null;
        boolean lunchBothPresent = lunchStart != null && lunchEnd != null;

        if (!lunchBothNull && !lunchBothPresent) {
            throw new IllegalArgumentException("Lunch start and lunch end must both be provided");
        }

        if (lunchBothPresent) {
            if (!lunchStart.isBefore(lunchEnd)) {
                throw new IllegalArgumentException("Lunch start must be before lunch end");
            }

            if (lunchStart.isBefore(workingStart) || lunchEnd.isAfter(workingEnd)) {
                throw new IllegalArgumentException("Lunch time must be inside barber working hours");
            }
        }
    }

    private void validateShiftInsideSalon(LocalTime barberStart, LocalTime barberEnd, LocalTime salonStart, LocalTime salonEnd) {
        if (barberStart.isBefore(salonStart) || barberEnd.isAfter(salonEnd)) {
            throw new IllegalArgumentException("Barber working time must be inside salon timing");
        }
    }

    private List<LocalDate> normalizeLeaves(List<LocalDate> leaves) {
        if (leaves == null) return new ArrayList<>();

        Set<LocalDate> unique = new LinkedHashSet<>();
        for (LocalDate date : leaves) {
            if (date == null) continue;
            if (date.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Past leave dates are not allowed");
            }
            unique.add(date);
        }

        return new ArrayList<>(unique);
    }

    private Set<DayOfWeek> normalizeWeeklyOffDays(Set<DayOfWeek> weeklyOffDays) {
        return weeklyOffDays == null ? new HashSet<>() : new HashSet<>(weeklyOffDays);
    }

    private boolean bookingFallsOutsideShift(Booking booking, LocalTime workingStart, LocalTime workingEnd, LocalTime lunchStart, LocalTime lunchEnd) {
        boolean outsideShift = booking.getStartTime().isBefore(workingStart) || booking.getEndTime().isAfter(workingEnd);

        boolean overlapsLunch = lunchStart != null && lunchEnd != null &&
                booking.getStartTime().isBefore(lunchEnd) &&
                booking.getEndTime().isAfter(lunchStart);

        return outsideShift || overlapsLunch;
    }

    private void handleConflictsIfNeeded(List<Booking> conflicts, Boolean autoCancel, String cancellationReason, String defaultReason) {
        if (conflicts == null || conflicts.isEmpty()) {
            return;
        }

        String finalReason = cancellationReason == null || cancellationReason.isBlank() ? defaultReason : cancellationReason.trim();

        if (autoCancel == null || !autoCancel) {
            throw new BookingConflictException(
                    BarberConflictResponse.builder()
                            .message("Conflicting bookings found")
                            .reason(finalReason)
                            .conflicts(conflicts.stream().map(this::toConflictBooking).toList())
                            .build()
            );
        }

        cancelBookingsAndNotify(conflicts, finalReason);
    }

    private BarberConflictResponse.ConflictBooking toConflictBooking(Booking booking) {
        return BarberConflictResponse.ConflictBooking.builder()
                .bookingId(booking.getId())
                .userId(booking.getUserId())
                .customerName(booking.getCustomerName())
                .bookingDate(booking.getBookingDate())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .build();
    }

    private void cancelBookingsAndNotify(List<Booking> bookings, String reason) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

        for (Booking booking : bookings) {
            booking.setStatus("CANCELLED");
            booking.setCancellationReason(reason);
            booking.setCancelledBy("OWNER");
            booking.setCancelledAt(java.time.LocalDateTime.now());
            bookingRepository.save(booking);

            String bookingInfo = "for " + booking.getBookingDate() + " at " + booking.getStartTime().format(timeFormatter);

            notificationService.createBookingCancelledNotification(
                    booking.getUserId(),
                    booking.getId(),
                    reason,
                    bookingInfo
            );
        }
    }
}