package com.project.render.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalonCardResponse {
    private String salonId;
    private String name;
    private String city;
    private String imageUrl;
    private List<String> services;
}
