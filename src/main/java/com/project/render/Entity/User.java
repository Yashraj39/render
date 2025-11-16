package com.project.render.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class User {
    @Id
    private String id;
    private String name;
    private String email;
    private String password;

}
