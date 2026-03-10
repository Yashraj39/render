package com.project.render.Entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Document(collection = "appointments")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Appointment {

    @Id
    private String id;

    private String ownerId;
    private String salonId;
    private String salonName;
    private String city;

    private List<String> barberName;

    private String customerName;
    private String customerEmail;

    private String serviceName;
    private Double price;

    private LocalDate appointmentDate;
    private LocalTime appointmentTime;

    private String status;
    private String paymentStatus;

    private String notes;
    private String billPdfUrl;
}