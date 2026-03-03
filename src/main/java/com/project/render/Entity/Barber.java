package com.project.render.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Document(collection="barbers")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Barber {
    @Id
    private String id;
    private String salonId; // which salon they belong to
    private String name;
    private boolean active; // optional, for shift/on leave
    private LocalTime workingStartTime;
    private LocalTime workingEndTime;
    private LocalTime lunchStart;    // 12:00
    private LocalTime lunchEnd;      // 13:00

    private List<LocalDate> leaves;
    private Set<DayOfWeek> weeklyOffDays;
}