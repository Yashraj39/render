package com.project.render.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SalonDetails {
    private String id;
    private String name;
    private String address;
    private String city;
    private String imageUrl;
    private String contact;
    private String email;
    private List<String> services;
}
