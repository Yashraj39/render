package com.project.render.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalTime;

@Document(collection = "appointments")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Appointment {
    @Id
    private String id;

    private String salonId;
    private String barberId;
    private String userId;

    private String serviceName;
    private String serviceType;
    private int price;

    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isCompleted;
}
