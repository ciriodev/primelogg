package com.cirio.primelog.controller;

import com.cirio.primelog.model.WaitlistEntry;
import com.cirio.primelog.repository.WaitlistRepository;
import com.cirio.primelog.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/waitlist")
@CrossOrigin(origins = "*")
public class WaitlistController {

    private final WaitlistRepository waitlistRepository;
    private final EmailService emailService;

    public WaitlistController(WaitlistRepository waitlistRepository,
                              EmailService emailService) {
        this.waitlistRepository = waitlistRepository;
        this.emailService       = emailService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> register(
            @RequestBody Map<String, String> body) {

        String email = body.get("email");

        if (email == null || email.isBlank() || !email.contains("@")) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Email inválido"));
        }

        email = email.trim().toLowerCase();

        // Si ya existe respondemos OK igualmente (no revelamos si estaba apuntado)
        if (waitlistRepository.existsByEmail(email)) {
            return ResponseEntity.ok(Map.of("status", "ok"));
        }

        // Guardamos en MongoDB
        waitlistRepository.save(new WaitlistEntry(email));
        System.out.println("📋 Nuevo email en waitlist: " + email);

        // Email de confirmación al usuario
        emailService.sendWaitlistConfirmation(email);

        // Notificación interna a ti
        emailService.sendInternalNotification(email);

        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
