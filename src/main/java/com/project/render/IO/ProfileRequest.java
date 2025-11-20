package com.project.render.IO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileRequest {

    @NotBlank(message = "Name should not be empty")
    private String name;

    @NotNull(message = "Email should not be empty")
    @NotBlank(message = "Email is required")
    @Email(message = "Enter valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 8, message = "Password must be atleast 6 characters")
    private String password;

}
