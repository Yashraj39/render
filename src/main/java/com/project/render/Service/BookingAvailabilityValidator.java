package com.project.render.Service;

import com.project.render.Entity.Barber;
import com.project.render.Entity.Salon;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Component
public class BookingAvailabilityValidator {

    public void validateDayAvailability(Barber barber, Salon salon, LocalDate date) {

        if (barber == null) throw new RuntimeException("Barber not found");
        if (salon == null) throw new RuntimeException("Salon not found");

        if (!barber.isActive()) throw new RuntimeException("Barber not available");

        if (barber.getLeaves() != null && barber.getLeaves().contains(date)) {
            throw new RuntimeException("Barber is on leave");
        }

        DayOfWeek day = date.getDayOfWeek();

        Set<DayOfWeek> salonOff = salon.getWeeklyOffDays();
        if (salonOff != null && salonOff.contains(day)) {
            throw new RuntimeException("Salon is closed on " + day);
        }

        Set<DayOfWeek> barberOff = barber.getWeeklyOffDays();
        if (barberOff != null && barberOff.contains(day)) {
            throw new RuntimeException("Barber is off on " + day);
        }
    }

    public void validateTimeWithinShift(Barber barber, LocalTime start, LocalTime end) {

        if (start == null || end == null) throw new RuntimeException("Invalid time");
        if (!start.isBefore(end)) throw new RuntimeException("Invalid slot");

        if (barber.getWorkingStartTime() != null && start.isBefore(barber.getWorkingStartTime())) {
            throw new RuntimeException("Outside working hours");
        }

        if (barber.getWorkingEndTime() != null && end.isAfter(barber.getWorkingEndTime())) {
            throw new RuntimeException("Outside working hours");
        }

        if (barber.getLunchStart() != null && barber.getLunchEnd() != null) {
            boolean inLunch = start.isBefore(barber.getLunchEnd()) && end.isAfter(barber.getLunchStart());
            if (inLunch) throw new RuntimeException("Lunch time");
        }
    }
}