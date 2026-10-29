package com.example.accountant.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record PublicAppointmentRequest(
        String name,
        String email,
        String phone,
        LocalDate date,
        LocalTime time,
        String message
) {
}
