
package com.example.accountant.repository;

import com.example.accountant.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository
        extends JpaRepository<Appointment, Long> {

    boolean existsByDateAndTime(LocalDate date, LocalTime time);

    List<Appointment> findAllByUser_IdOrderByDateDescTimeDesc(Long userId);

    List<Appointment> findAllByOrderByDateDescTimeDesc();

    List<Appointment> findAllByDateBetweenOrderByDateAscTimeAsc(
            LocalDate start, LocalDate end);

    List<Appointment> findAllByUser_Email(String email);

    Optional<Appointment> findByIdAndUser_Email(
            Long id, String email);
}
