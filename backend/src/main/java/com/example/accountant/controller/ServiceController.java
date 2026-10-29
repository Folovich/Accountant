package com.example.accountant.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class ServiceController {

    @GetMapping("/api/services")
    public List<Map<String, String>> getServices() {
        return List.of(
                Map.of(
                        "name", "Налоговая консультация",
                        "description", "Консультация по вопросам налогов"
                ),
                Map.of(
                        "name", "Ведение бухгалтерии",
                        "description", "Помощь с бухгалтерским учётом"
                ),
                Map.of(
                        "name", "Подготовка документов",
                        "description", "Подготовка бухгалтерской документации"
                )
        );
    }
}