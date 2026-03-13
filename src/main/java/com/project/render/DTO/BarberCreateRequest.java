package com.project.render.DTO;

import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Data
public class BarberCreateRequest {
    private String name;
    private Boolean active;
    private LocalTime workingStartTime;
    private LocalTime workingEndTime;
    private LocalTime lunchStart;
    private LocalTime lunchEnd;
    private List<LocalDate> leaves;
    private Set<DayOfWeek> weeklyOffDays;
}