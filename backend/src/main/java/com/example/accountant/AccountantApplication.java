package com.example.accountant;

import com.example.accountant.model.AppUser;
import com.example.accountant.model.Role;
import com.example.accountant.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Locale;

@SpringBootApplication
public class AccountantApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountantApplication.class, args);
    }

    @Bean
    CommandLineRunner createDefaultAccountant(
            UserRepository users,
            PasswordEncoder passwordEncoder,
            @Value("${app.accountant.email:accountant@example.com}") String email,
            @Value("${app.accountant.password:42Abc4242}") String password) {
        return args -> {
            String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
            var existing = users.findByEmail(normalizedEmail);
            if (existing.isEmpty()) {
                users.save(new AppUser(
                        normalizedEmail,
                        passwordEncoder.encode(password),
                        Role.ACCOUNTANT));
            } else if (existing.get().getPassword().equals(password)) {
                existing.get().setPassword(passwordEncoder.encode(password));
                users.save(existing.get());
            }
        };
    }
}
