package com.project.render.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class BarberConflictResponse {
    private String message;
    private String reason;
    private List<ConflictBooking> conflicts;

    @Data
    @Builder
    @AllArgsConstructor
    public static class ConflictBooking {
        private String bookingId;
        private String userId;
        private String customerName;
        private LocalDate bookingDate;
        private LocalTime startTime;
        private LocalTime endTime;
    }
}