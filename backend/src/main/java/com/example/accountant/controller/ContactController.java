package com.example.accountant.controller;

import com.example.accountant.dto.PublicContactRequest;
import com.example.accountant.model.ContactMessage;
import com.example.accountant.repository.ContactMessageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final ContactMessageRepository messages;

    public ContactController(ContactMessageRepository messages) {
        this.messages = messages;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody PublicContactRequest request) {
        if (isBlank(request.name()) || isBlank(request.email())
                || !EMAIL_PATTERN.matcher(request.email().trim()).matches()
                || isBlank(request.message())) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Укажите имя, корректный email и сообщение"));
        }

        ContactMessage saved = messages.save(new ContactMessage(
                request.name().trim(),
                request.email().trim(),
                request.topic() == null ? "" : request.topic().trim(),
                request.message().trim()));
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public List<ContactMessage> getAll() {
        return messages.findAllByOrderByCreatedAtDesc();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
