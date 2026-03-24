package com.project.render.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminLoginRequest {
    private String email;
    private String password;
}
