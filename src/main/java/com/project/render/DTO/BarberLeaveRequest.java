package com.project.render.DTO;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BarberLeaveRequest {
    private List<LocalDate> leaves;
    private Boolean autoCancelConflictingBookings;
    private String cancellationReason;
}