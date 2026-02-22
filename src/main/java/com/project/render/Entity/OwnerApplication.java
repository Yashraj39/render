package com.project.render.Entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "owner_applications")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OwnerApplication {

    @Id
    private String id;

    private String userId;
    private String phone;
    private String email;
    private String aadhaarUrl;

    private Boolean termsAccepted;

    private String status;
    private Date createdAt;
    private Date reviewedAt;
    private String reviewedBy;
    private String adminNote;

}