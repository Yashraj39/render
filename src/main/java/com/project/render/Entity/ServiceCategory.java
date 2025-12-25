package com.project.render.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "service_categories")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceCategory {

    @Id
    private String id;
    private String name;
    private String description;
    private List<String> serviceIds;

}
