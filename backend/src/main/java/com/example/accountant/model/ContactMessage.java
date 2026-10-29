package com.example.accountant.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class ContactMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    private String topic;

    @Column(nullable = false, length = 4000)
    private String message;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected ContactMessage() {
    }

    public ContactMessage(String name, String email, String topic, String message) {
        this.name = name;
        this.email = email;
        this.topic = topic;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getTopic() {
        return topic;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
