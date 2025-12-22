package com.project.render.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "servicetypes")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceType {

    private String name;
    private String category; //eg men/woman/child
    private String description;
    private int price;
    private int time;

}
