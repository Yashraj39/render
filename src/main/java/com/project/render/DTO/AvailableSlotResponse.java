package com.project.render.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalTime;

@Data
@AllArgsConstructor
public class AvailableSlotResponse {

    private LocalTime startTime;
    private LocalTime endTime;

}

