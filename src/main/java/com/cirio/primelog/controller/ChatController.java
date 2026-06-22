package com.cirio.primelog.controller;

import com.cirio.primelog.service.GeminiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*") // Permite que nuestro HTML conecte sin quejas de seguridad
public class ChatController {

    private final GeminiService geminiService;

    public ChatController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> hablarConIA(@RequestBody Map<String, String> payload) {
        String pregunta = payload.get("mensaje");
        String respuestaIA = geminiService.procesarPregunta(pregunta);

        // Devolvemos la respuesta formateada en JSON
        return ResponseEntity.ok(Map.of("respuesta", respuestaIA));
    }
}