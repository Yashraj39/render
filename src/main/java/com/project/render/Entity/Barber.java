package com.project.render.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="barbers")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Barber {
    @Id
    private String id;
    private String salonId; // which salon they belong to
    private String name;
    private boolean isActive; // optional, for shift/on leave
}