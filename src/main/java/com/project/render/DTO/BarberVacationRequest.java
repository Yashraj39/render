package com.project.render.DTO;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BarberVacationRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean autoCancelConflictingBookings;
    private String cancellationReason;
}