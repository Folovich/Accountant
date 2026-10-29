
package com.example.accountant.service;

import com.example.accountant.model.AppUser;
import com.example.accountant.model.Role;
import com.example.accountant.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository repository,
            PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public AppUser register(String email, String password) {
        email = normalizeEmail(email);

        if (email.isBlank() || !EMAIL_PATTERN.matcher(email).matches()
                || password == null || password.length() < 8) {
            throw new IllegalArgumentException(
                    "Укажите корректный email и пароль не короче 8 символов");
        }

        if (repository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Этот email уже зарегистрирован");
        }

        AppUser user = new AppUser(
                email,
                passwordEncoder.encode(password),
                Role.USER
        );

        return repository.save(user);
    }

    public static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
