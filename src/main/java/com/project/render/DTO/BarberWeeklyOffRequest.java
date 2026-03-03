package com.project.render.DTO;

import lombok.Data;

import java.time.DayOfWeek;
import java.util.Set;

@Data
public class BarberWeeklyOffRequest {
    private Set<DayOfWeek> weeklyOffDays;
}
