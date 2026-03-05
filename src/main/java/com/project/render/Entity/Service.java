package com.project.render.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "services")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Service {

    @Id
    private String id;
    private String name;
    private String genderCategory; //eg men/woman/child
    private String description;
    private int price;
    private int time;
    private String imageUrl;

    private String categoryId;

    private String salonId;

}
