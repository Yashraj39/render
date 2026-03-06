package com.project.render.Repository;

import com.project.render.Entity.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends MongoRepository<Booking, String> {

    List<Booking> findByBarberIdAndBookingDate(String barberId, LocalDate bookingDate);

    List<Booking> findByBarberIdAndBookingDateAndStatus(String barberId, LocalDate bookingDate, String confirmed);

    List<Booking> findBySalonId(String salonId);

    List<Booking> findBySalonIdAndBookingDate(String salonId, LocalDate date);

    List<Booking> findByUserId(String userId);

    Optional<Booking> findByIdAndUserId(String id, String userId);
}


