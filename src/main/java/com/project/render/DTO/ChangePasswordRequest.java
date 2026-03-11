package com.project.render.DTO;

import lombok.Data;

@Data
public class ChangePasswordRequest {

    private String userId;
    private String currentPassword;
    private String newPassword;

}
