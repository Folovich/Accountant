
package com.example.accountant.controller;

import com.example.accountant.model.AppUser;
import com.example.accountant.repository.UserRepository;
import com.example.accountant.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository repository;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository contextRepository;

    public AuthController(
            AuthService authService,
            UserRepository repository,
            AuthenticationManager authenticationManager,
            SecurityContextRepository contextRepository) {
        this.authService = authService;
        this.repository = repository;
        this.authenticationManager = authenticationManager;
        this.contextRepository = contextRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody Map<String, String> body) {
        try {
            AppUser user = authService.register(
                    body.getOrDefault("email", ""),
                    body.getOrDefault("password", "")
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    Map.of(
                            "id", user.getId(),
                            "email", user.getEmail(),
                            "role", user.getRole().name()
                    )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> body,
            HttpServletRequest request,
            HttpServletResponse response) {

        try {
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    body.getOrDefault("email", "")
                                            .trim().toLowerCase(),
                                    body.getOrDefault("password", "")
                            )
                    );

            SecurityContext context =
                    SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            request.getSession(true);
            contextRepository.saveContext(
                    context, request, response);


            request.getSession(true);
            contextRepository.saveContext(
                    context, request, response);

            System.out.println("SESSION ID: " + request.getSession().getId());
            System.out.println("AUTH: " + SecurityContextHolder.getContext().getAuthentication());

            AppUser user = repository.findByEmail(
                    authentication.getName()).orElseThrow();
            return ResponseEntity.ok(Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "role", user.getRole().name()
            ));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Неверный email или пароль"));
        }
    }
}
