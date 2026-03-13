package com.project.render.DTO;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class BarberTemporaryInactiveRequest {
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean autoCancelConflictingBookings;
    private String reason;
}