package com.project.render.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Document(collection = "salons")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Salon {

    @Id
    private String id;

    @Indexed
    private String salonOwnerId;

    private String name;
    private String city;
    private String address;
    private String contact;
    private String salonEmail;

    private LocalTime opentime;
    private LocalTime closetime;

    private String mapLink;

    private String imageUrl;
    private String interiorImageUrl;
    private String exteriorImageUrl;
    private String ownerPhotoUrl;

    private DocumentType documentType;
    private String documentUrl;

    private List<String> barbersIds;
    private List<String> serviceIds;

    private boolean isVerified = false;   // default false
    private String verificationStatus; // PENDING, APPROVED, REJECTED
    private String rejectionReason;
    private String verifiedByAdminId;
    private String adminNote;
    private Set<DayOfWeek> weeklyOffDays;

}
