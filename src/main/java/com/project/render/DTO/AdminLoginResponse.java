package com.project.render.DTO;

import lombok.*;

@Builder
@Getter
@Setter
public class AdminLoginResponse {
    private String userId;
    private String name;
    private String email;
    private String role;
}
