package com.example.accountant.dto;

public record PublicContactRequest(
        String name,
        String email,
        String topic,
        String message
) {
}
