package com.project.render.Exception;

import com.project.render.DTO.BarberConflictResponse;
import lombok.Getter;

@Getter
public class BookingConflictException extends RuntimeException {
    private final BarberConflictResponse response;

    public BookingConflictException(BarberConflictResponse response) {
        super(response.getMessage());
        this.response = response;
    }
}