package com.project.render.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalTime;
import java.util.List;

@Document(collection = "salons")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Salon {

    @Id
    private String id;
    @Indexed(unique = true)
    private String salonOwnerId;
    private String name;
    private String city;
    private String address;
    private LocalTime opentime;
    private LocalTime closetime;
    private String imageUrl;

    private List<String> serviceIds;

}
