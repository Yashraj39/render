package com.project.render.DTO;

import lombok.Data;

@Data
public class ServiceUpdateRequest {
    private String name;
    private String genderCategory;
    private String description;
    private Integer price;
    private Integer time;
}