package com.project.render.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OwnerSummaryResponse {
    private String userId;
    private String name;
    private String email;
    private String role;
    private Boolean ownerFrozen;
    private String freezeReason;
    private Date ownerFrozenAt;
}