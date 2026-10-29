
package com.example.accountant.service;

import com.example.accountant.model.AppUser;
import com.example.accountant.model.Role;
import com.example.accountant.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository repository;

    public AuthService(UserRepository repository) {
        this.repository = repository;
    }

    public AppUser register(String email, String password) {
        email = email.trim().toLowerCase();

        if (email.isBlank() || password.length() < 8) {
            throw new IllegalArgumentException(
                    "Укажите email и пароль не короче 8 символов");
        }

        if (repository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Этот email уже зарегистрирован");
        }

        AppUser user = new AppUser(
                email,
                password,
                Role.USER
        );

        return repository.save(user);
    }
}
