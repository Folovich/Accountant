package com.example.accountant.controller;

import com.example.accountant.dto.AppointmentResponse;
import com.example.accountant.dto.PublicAppointmentRequest;
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
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final AppointmentRepository appointments;
    private final UserRepository users;

    public AppointmentController(
            AppointmentRepository appointments,
            UserRepository users) {
        this.appointments = appointments;
        this.users = users;
    }

    @GetMapping
    public List<AppointmentResponse> getAll() {
        return appointments.findAllByOrderByDateDescTimeDesc().stream()
                .map(AppointmentResponse::full)
                .toList();
    }

    @GetMapping("/mine")
    public List<AppointmentResponse> getMine(Authentication authentication) {
        return appointments.findAllByUser_IdOrderByDateDescTimeDesc(
                        currentUser(authentication).getId())
                .stream()
                .map(AppointmentResponse::full)
                .toList();
    }

    @GetMapping("/week")
    public ResponseEntity<?> getWeek(
            @RequestParam LocalDate start,
            Authentication authentication) {
        AppUser user = currentUser(authentication);
        boolean isAccountant = user.getRole() == Role.ACCOUNTANT;

        List<AppointmentResponse> schedule = appointments
                .findAllByDateBetweenOrderByDateAscTimeAsc(start, start.plusDays(6))
                .stream()
                .map(appointment -> isAccountant
                        || (appointment.getUser() != null
                        && appointment.getUser().getId().equals(user.getId()))
                        ? AppointmentResponse.full(appointment)
                        : AppointmentResponse.busy(appointment))
                .toList();
        return ResponseEntity.ok(schedule);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(
            @PathVariable Long id,
            Authentication authentication) {
        AppUser user = currentUser(authentication);
        return appointments.findById(id)
                .filter(appointment -> user.getRole().name().equals("ACCOUNTANT")
                        || (appointment.getUser() != null
                        && appointment.getUser().getId().equals(user.getId())))
                .<ResponseEntity<?>>map(appointment ->
                        ResponseEntity.ok(AppointmentResponse.full(appointment)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody PublicAppointmentRequest request,
            Authentication authentication) {
        if (isBlank(request.name()) || isBlank(request.email())
                || !EMAIL_PATTERN.matcher(request.email().trim()).matches()
                || request.date() == null || request.time() == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Укажите имя, корректный email, дату и время"));
        }
        if (request.date().isBefore(LocalDate.now())) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Дата уже прошла"));
        }
        if (appointments.existsByDateAndTime(request.date(), request.time())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("error", "Это время уже занято. Выберите другое."));
        }

        Appointment appointment = new Appointment();
        appointment.setName(request.name().trim());
        appointment.setEmail(request.email().trim());
        appointment.setPhone(request.phone());
        appointment.setDate(request.date());
        appointment.setTime(request.time());
        appointment.setMessage(request.message());
        if (authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            appointment.setUser(currentUser(authentication));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AppointmentResponse.full(appointments.save(appointment)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!appointments.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        appointments.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private AppUser currentUser(Authentication authentication) {
        return users.findByEmail(authentication.getName()).orElseThrow();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
