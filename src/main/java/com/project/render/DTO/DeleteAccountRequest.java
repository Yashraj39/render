package com.project.render.DTO;

import lombok.Data;

@Data
public class DeleteAccountRequest {
    private String userId;
    private String password;
}
