package com.project.render.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "services")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Service {

    private String name;
    private String genderCategory; //eg men/woman/child
    private String description;
    private int price;
    private int time;

}
