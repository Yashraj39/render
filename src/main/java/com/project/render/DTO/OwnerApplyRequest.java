package com.project.render.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OwnerApplyRequest {
    private String userId;
    private String phone;
    private String email;
    private String aadhaarUrl;
    private Boolean termsAccepted;
}
