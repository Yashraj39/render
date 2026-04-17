package com.project.render.Repository;

import com.project.render.Entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends MongoRepository<Booking, String> {

    List<Booking> findByBarberIdAndBookingDate(String barberId, LocalDate bookingDate);

    List<Booking> findByBarberIdAndBookingDateAndStatus(String barberId, LocalDate bookingDate, String status);

    List<Booking> findByBarberIdAndStatus(String barberId, String status);

    List<Booking> findByBarberIdAndBookingDateInAndStatus(String barberId, List<LocalDate> bookingDates, String status);

    List<Booking> findBySalonId(String salonId);

    List<Booking> findBySalonIdAndBookingDate(String salonId, LocalDate date);

    List<Booking> findByUserId(String userId);

    Optional<Booking> findByIdAndUserId(String id, String userId);

    Page<Booking> findBySalonIdIn(List<String> salonIds, Pageable pageable);

    Page<Booking> findBySalonId(String salonId, Pageable pageable);

    Page<Booking> findBySalonIdInAndBarberId(List<String> salonIds, String barberId, Pageable pageable);

    Page<Booking> findBySalonIdAndBarberId(String salonId, String barberId, Pageable pageable);

    Page<Booking> findBySalonIdInAndBookingDate(List<String> salonIds, LocalDate bookingDate, Pageable pageable);

    Page<Booking> findBySalonIdAndBookingDate(String salonId, LocalDate bookingDate, Pageable pageable);

    Page<Booking> findBySalonIdInAndStatus(List<String> salonIds, String status, Pageable pageable);

    Page<Booking> findBySalonIdAndStatus(String salonId, String status, Pageable pageable);

    Page<Booking> findBySalonIdInAndBarberIdAndBookingDate(
            List<String> salonIds, String barberId, LocalDate bookingDate, Pageable pageable
    );

    Page<Booking> findBySalonIdAndBarberIdAndBookingDate(
            String salonId, String barberId, LocalDate bookingDate, Pageable pageable
    );

    Page<Booking> findBySalonIdInAndBarberIdAndStatus(
            List<String> salonIds, String barberId, String status, Pageable pageable
    );

    Page<Booking> findBySalonIdAndBarberIdAndStatus(
            String salonId, String barberId, String status, Pageable pageable
    );

    Page<Booking> findBySalonIdInAndBookingDateAndStatus(
            List<String> salonIds, LocalDate bookingDate, String status, Pageable pageable
    );

    Page<Booking> findBySalonIdAndBookingDateAndStatus(
            String salonId, LocalDate bookingDate, String status, Pageable pageable
    );

    Page<Booking> findBySalonIdInAndBarberIdAndBookingDateAndStatus(
            List<String> salonIds, String barberId, LocalDate bookingDate, String status, Pageable pageable
    );

    Page<Booking> findBySalonIdAndBarberIdAndBookingDateAndStatus(
            String salonId, String barberId, LocalDate bookingDate, String status, Pageable pageable
    );

    // Soft delete compatible methods for user booking history
    @Query("{ 'userId': ?0, '$or': [ { 'userDeleted': false }, { 'userDeleted': { '$exists': false } } ] }")
    List<Booking> findVisibleByUserId(String userId);

    @Query("{ '_id': ?0, 'userId': ?1, '$or': [ { 'userDeleted': false }, { 'userDeleted': { '$exists': false } } ] }")
    Optional<Booking> findVisibleByIdAndUserId(String id, String userId);
}