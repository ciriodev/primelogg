package com.cirio.primelog.service;

import com.cirio.primelog.model.ErrorLog;
import com.cirio.primelog.repository.ErrorLogRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final ErrorLogRepository repository;

    public GeminiService(ErrorLogRepository repository) {
        this.repository = repository;
    }

    public String procesarPregunta(String preguntaUsuario) {
        // 1. Extraer TODO el historial de MongoDB (El contexto RAG)
        List<ErrorLog> historial = repository.findAll();
        StringBuilder contexto = new StringBuilder();

        for (ErrorLog error : historial) {
            contexto.append("- Título: ").append(error.getTitulo())
                    .append(" | Contexto: ").append(error.getDescripcionContexto())
                    .append(" | Solución: ").append(error.getSolucionAplicada()).append("\n");
        }

        // 2. Construir el Prompt Maestro
        String promptFinal = "Eres PrimeBot, el asistente de IA integrado en PrimeLog. " +
                "Tu objetivo es ayudar al programador a resolver problemas basándote estrictamente en su historial de errores previos.\n\n" +
                "HISTORIAL DE ERRORES RESUELTOS DEL PROGRAMADOR:\n" + contexto.toString() + "\n\n" +
                "INSTRUCCIONES:\n" +
                "1. Si la pregunta está relacionada con el historial, responde resumiendo la solución aplicada en el pasado de forma directa.\n" +
                "2. Si la pregunta NO está en el historial, responde basándote en tus conocimientos generales de programación, pero avisa de que es un error nuevo que no está registrado.\n\n" +
                "PREGUNTA DEL USUARIO: " + preguntaUsuario;

        // 3. Configurar la llamada HTTP a Google Gemini
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> parts = new HashMap<>();
        parts.put("text", promptFinal);
        Map<String, Object> contents = new HashMap<>();
        contents.put("parts", new Object[]{parts});
        requestBody.put("contents", new Object[]{contents});

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // 4. Ejecutar la llamada y extraer la respuesta
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl + apiKey, entity, Map.class);
            Map<String, Object> body = response.getBody();
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> resParts = (List<Map<String, Object>>) content.get("parts");

            return (String) resParts.get(0).get("text");
        } catch (Exception e) {
            return "Error en los circuitos de PrimeBot: No he podido contactar con el servidor base. (" + e.getMessage() + ")";
        }
    }
    public String generarEtiqueta(String titulo, String contexto, String solucion) {
        String prompt = "Eres un clasificador técnico experto. Analiza este error y devuelve ÚNICAMENTE UNA PALABRA CLAVE que represente la tecnología principal (Ej: Java, MongoDB, Spring, Python, Frontend, Git, etc). NO añadas puntos, ni explicaciones, solo la palabra exacta.\n" +
                "Título: " + titulo + "\nContexto: " + contexto + "\nSolución: " + solucion;

        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> parts = new HashMap<>();
        parts.put("text", prompt);
        Map<String, Object> contents = new HashMap<>();
        contents.put("parts", new Object[]{parts});
        requestBody.put("contents", new Object[]{contents});

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl + apiKey, entity, Map.class);
            Map<String, Object> body = response.getBody();
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> resParts = (List<Map<String, Object>>) content.get("parts");

            // Extraemos la palabra y le quitamos espacios invisibles o saltos de línea
            return ((String) resParts.get(0).get("text")).trim();
        } catch (Exception e) {
            return "General"; // Si la IA falla, ponemos una etiqueta por defecto para que no pete la BD
        }
    }
}