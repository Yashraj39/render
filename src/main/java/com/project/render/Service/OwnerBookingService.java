package com.project.render.Service;

import com.project.render.DTO.OwnerBookingFilterOptionDto;
import com.project.render.DTO.OwnerBookingRowResponse;
import com.project.render.DTO.OwnerBookingsPageResponse;
import com.project.render.Entity.Barber;
import com.project.render.Entity.Booking;
import com.project.render.Entity.CartItem;
import com.project.render.Entity.Salon;
import com.project.render.Entity.User;
import com.project.render.Repository.BarberRepository;
import com.project.render.Repository.BookingRepository;
import com.project.render.Repository.SalonRepository;
import com.project.render.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OwnerBookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private SalonRepository salonRepository;

    @Autowired
    private BarberRepository barberRepository;

    @Autowired
    private UserRepository userRepository;

    public OwnerBookingsPageResponse getOwnerBookings(
            String ownerId,
            String salonId,
            LocalDate date,
            String barberId,
            String status,
            int page,
            int size
    ) {

        System.out.println("PRODUCTION TEST VERSION 2 IS DEPLOYED!!");

        List<Salon> ownerSalons = salonRepository.findBySalonOwnerId(ownerId);

        if (ownerSalons.isEmpty()) {
            return OwnerBookingsPageResponse.builder()
                    .totalBookings(0)
                    .currentPage(page)
                    .pageSize(size)
                    .totalPages(0)
                    .bookings(Collections.emptyList())
                    .salons(Collections.emptyList())
                    .barbers(Collections.emptyList())
                    .build();
        }

        List<String> ownerSalonIds = ownerSalons.stream()
                .map(Salon::getId)
                .toList();

        String selectedSalonId = blankToNull(salonId);
        String selectedBarberId = blankToNull(barberId);
        String normalizedStatus = normalizeStatus(status);

        Map<String, Salon> salonMap = ownerSalons.stream()
                .collect(Collectors.toMap(Salon::getId, s -> s));

        List<Booking> filteredBookings = getFilteredBookings(
                ownerSalonIds,
                selectedSalonId,
                selectedBarberId,
                date,
                normalizedStatus
        );

        filteredBookings = filteredBookings.stream()
                .sorted(
                        Comparator.comparing(Booking::getBookingDate, Comparator.nullsLast(Comparator.reverseOrder()))
                                .thenComparing(
                                        booking -> toDisplayTime(booking.getStartTime()),
                                        Comparator.nullsLast(Comparator.reverseOrder())
                                )
                )
                .toList();

        int totalBookings = filteredBookings.size();
        int totalPages = totalBookings == 0 ? 0 : (int) Math.ceil((double) totalBookings / size);

        int fromIndex = Math.min(page * size, totalBookings);
        int toIndex = Math.min(fromIndex + size, totalBookings);

        List<Booking> pagedBookings = filteredBookings.subList(fromIndex, toIndex);

        Set<String> barberIds = pagedBookings.stream()
                .map(Booking::getBarberId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, Barber> barberMap = barberRepository.findAllById(barberIds).stream()
                .collect(Collectors.toMap(Barber::getId, b -> b));

        Set<String> userIds = pagedBookings.stream()
                .map(Booking::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, User> userMap = userIds.stream()
                .map(userRepository::findByUserId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(User::getUserId, u -> u));

        List<OwnerBookingRowResponse> rows = pagedBookings.stream()
                .map(booking -> mapToRow(booking, salonMap, barberMap, userMap))
                .toList();

        List<OwnerBookingFilterOptionDto> salonOptions = ownerSalons.stream()
                .map(s -> OwnerBookingFilterOptionDto.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .build())
                .toList();

        List<OwnerBookingFilterOptionDto> barberOptions = buildBarberOptions(ownerSalons, selectedSalonId);

        return OwnerBookingsPageResponse.builder()
                .totalBookings(totalBookings)
                .currentPage(page)
                .pageSize(size)
                .totalPages(totalPages)
                .bookings(rows)
                .salons(salonOptions)
                .barbers(barberOptions)
                .build();
    }

    private LocalTime toDisplayTime(LocalTime time) {
        if (time == null) return null;
        return time.minusHours(5).minusMinutes(30);
    }

    private List<Booking> getFilteredBookings(
            List<String> ownerSalonIds,
            String salonId,
            String barberId,
            LocalDate date,
            String status
    ) {
        if (salonId != null && !ownerSalonIds.contains(salonId)) {
            throw new RuntimeException("Unauthorized salon access");
        }

        List<Booking> rawBookings;

        if (salonId != null) {
            rawBookings = bookingRepository.findBySalonId(salonId);
        } else {
            rawBookings = ownerSalonIds.stream()
                    .flatMap(id -> bookingRepository.findBySalonId(id).stream())
                    .toList();
        }

        return rawBookings.stream()
                .filter(booking -> barberId == null || barberId.equals(booking.getBarberId()))
                .filter(booking -> date == null || date.equals(booking.getBookingDate()))
                .filter(booking -> {
                    if (status == null) return true;
                    return status.equalsIgnoreCase(resolveDisplayStatus(booking));
                })
                .toList();
    }

    private OwnerBookingRowResponse mapToRow(
            Booking booking,
            Map<String, Salon> salonMap,
            Map<String, Barber> barberMap,
            Map<String, User> userMap
    ) {
        Salon salon = salonMap.get(booking.getSalonId());
        Barber barber = barberMap.get(booking.getBarberId());
        User user = userMap.get(booking.getUserId());

        List<String> services = booking.getServices() == null
                ? Collections.emptyList()
                : booking.getServices().stream()
                .map(CartItem::getServiceName)
                .filter(Objects::nonNull)
                .toList();

        return OwnerBookingRowResponse.builder()
                .bookingId(booking.getId())
                .userId(booking.getUserId())
                .customerName(booking.getCustomerName())
                .customerEmail(user != null ? user.getEmail() : null)
                .salonId(booking.getSalonId())
                .salonName(salon != null ? salon.getName() : null)
                .city(salon != null ? salon.getCity() : null)
                .barberId(booking.getBarberId())
                .barberName(barber != null ? barber.getName() : null)
                .bookingDate(booking.getBookingDate())
                .startTime(toDisplayTime(booking.getStartTime()))
                .endTime(toDisplayTime(booking.getEndTime()))
                .serviceNames(services)
                .totalPrice(booking.getTotalPrice())
                .totalTime(booking.getTotalTime())
                .status(resolveDisplayStatus(booking))
                .paymentStatus(booking.getPaymentStatus())
                .build();
    }

    private List<OwnerBookingFilterOptionDto> buildBarberOptions(List<Salon> ownerSalons, String selectedSalonId) {
        Set<String> barberIds = new HashSet<>();

        for (Salon salon : ownerSalons) {
            if (selectedSalonId != null && !selectedSalonId.isBlank() && !selectedSalonId.equals(salon.getId())) {
                continue;
            }
            if (salon.getBarbersIds() != null) {
                barberIds.addAll(salon.getBarbersIds());
            }
        }

        return barberRepository.findAllById(barberIds).stream()
                .map(b -> OwnerBookingFilterOptionDto.builder()
                        .id(b.getId())
                        .name(b.getName())
                        .build())
                .sorted(Comparator.comparing(OwnerBookingFilterOptionDto::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private String resolveDisplayStatus(Booking booking) {
        if (booking.getStatus() == null) {
            return "PENDING";
        }

        if ("CANCELLED".equalsIgnoreCase(booking.getStatus())) {
            return "CANCELLED";
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        LocalTime endTime = toDisplayTime(booking.getEndTime());

        if (booking.getBookingDate() != null && endTime != null) {
            boolean completed =
                    booking.getBookingDate().isBefore(today) ||
                            (booking.getBookingDate().isEqual(today) && endTime.isBefore(now));

            if (completed) {
                return "COMPLETED";
            }
        }

        if ("CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
            return "CONFIRMED";
        }

        return booking.getStatus().toUpperCase();
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank() || status.equalsIgnoreCase("ALL")) {
            return null;
        }

        return status.trim().toUpperCase();
    }

    private String blankToNull(String value) {
        if (value == null || value.trim().isBlank() || value.equalsIgnoreCase("ALL")) {
            return null;
        }
        return value.trim();
    }
}