package com.example.accountant.dto;

import com.example.accountant.model.Appointment;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.LocalTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AppointmentResponse(
        Long id,
        LocalDate date,
        LocalTime time,
        String status,
        String name,
        String email,
        String phone,
        String message
) {
    public static AppointmentResponse full(Appointment a) {
        return new AppointmentResponse(
                a.getId(),
                a.getDate(),
                a.getTime(),
                "BOOKED",
                a.getName(),
                a.getEmail(),
                a.getPhone(),
                a.getMessage()
        );
    }

    public static AppointmentResponse busy(Appointment a) {
        return new AppointmentResponse(
                null,
                a.getDate(),
                a.getTime(),
                "BOOKED",
                null,
                null,
                null,
                null
        );
    }
}