package com.project.render.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TemporaryInactiveSlot {
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String reason;
}