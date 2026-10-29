package com.example.accountant.controller;

import com.example.accountant.dto.AppointmentResponse;
import com.example.accountant.model.AppUser;
import com.example.accountant.model.Appointment;
import com.example.accountant.model.Role;
import com.example.accountant.repository.AppointmentRepository;
import com.example.accountant.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentRepository appointments;
    private final UserRepository users;

    public AppointmentController(
            AppointmentRepository appointments,
            UserRepository users) {
        this.appointments = appointments;
        this.users = users;
    }

    private AppUser currentUser(Authentication auth) {
        return users.findByEmail(auth.getName())
                .orElseThrow();
    }

    private boolean isAccountant(AppUser user) {
        return user.getRole() == Role.ACCOUNTANT;
    }

    @GetMapping
    public List<AppointmentResponse> getAll(
            Authentication auth) {

        AppUser user = currentUser(auth);

        return appointments.findAll().stream()
                .map(a -> {
                    if (isAccountant(user)) {
                        return AppointmentResponse.full(a);
                    }

                    if (a.getUser().getId().equals(user.getId())) {
                        return AppointmentResponse.full(a);
                    }

                    return AppointmentResponse.busy(a);
                })
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(
            @PathVariable Long id,
            Authentication auth) {

        AppUser user = currentUser(auth);

        return appointments.findById(id)
                .filter(a -> isAccountant(user)
                        || a.getUser().getId().equals(user.getId()))
                .<ResponseEntity<?>>map(a ->
                        ResponseEntity.ok(
                                AppointmentResponse.full(a)))
                .orElseGet(() ->
                        ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody Appointment appointment,
            Authentication auth) {

        AppUser user = currentUser(auth);

        if (appointment.getName() == null
                || appointment.getName().isBlank()
                || appointment.getPhone() == null
                || appointment.getPhone().isBlank()
                || appointment.getDate() == null
                || appointment.getTime() == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Заполните обязательные поля"));
        }

        if (appointment.getDate().isBefore(LocalDate.now())) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Дата уже прошла"));
        }

        if (appointments.existsByDateAndTime(
                appointment.getDate(), appointment.getTime())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("error", "Время уже занято"));
        }

        appointment.setUser(user);

        Appointment saved = appointments.save(appointment);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AppointmentResponse.full(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable Long id,
            Authentication auth) {

        AppUser user = currentUser(auth);

        var appointment = appointments.findById(id);

        if (appointment.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Appointment a = appointment.get();

        if (!isAccountant(user)
                && !a.getUser().getId().equals(user.getId())) {
            return ResponseEntity.notFound().build();
        }

        appointments.delete(a);

        return ResponseEntity.noContent().build();
    }
}