package com.cirio.primelog.controller;

import com.cirio.primelog.model.ErrorLog;
import com.cirio.primelog.model.User;
import com.cirio.primelog.repository.ErrorLogRepository;
import com.cirio.primelog.repository.UserRepository;
import com.cirio.primelog.service.GeminiService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/errores")
@CrossOrigin(origins = "*")
public class ErrorLogController {

    private final ErrorLogRepository repository;
    private final GeminiService geminiService;
    private final UserRepository userRepository;

    public ErrorLogController(ErrorLogRepository repository, GeminiService geminiService, UserRepository userRepository) {
        this.repository = repository;
        this.geminiService = geminiService;
        this.userRepository = userRepository;
    }

    // Método radar: intercepta el token de seguridad y nos devuelve al usuario exacto
    private User getUsuarioAutenticado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElse(null);
    }

    @GetMapping
    public ResponseEntity<List<ErrorLog>> obtenerErrores() {
        User usuario = getUsuarioAutenticado();
        if (usuario == null) return ResponseEntity.status(401).build();

        // ¡Magia SaaS! Solo le devolvemos la lista de SUS errores
        return ResponseEntity.ok(repository.findByUsuarioId(usuario.getId()));
    }

    @PostMapping
    public ResponseEntity<?> guardarError(@RequestBody ErrorLog nuevoError) {
        User usuario = getUsuarioAutenticado();
        if (usuario == null) return ResponseEntity.status(401).build();

        // 1. COMPROBAR EL LÍMITE DEL PLAN FREEMIUM
        if ("FREE".equals(usuario.getRole())) {
            long totalErrores = repository.countByUsuarioId(usuario.getId());
            if (totalErrores >= 20) {
                // Devolvemos un 403 con un mensaje claro para que el frontend sepa que debe mostrar el modal de Stripe
                return ResponseEntity.status(403).body("Has alcanzado el límite de 20 errores del plan gratuito. Pásate a PRO para almacenamiento ilimitado.");
            }
        }

        // 2. Si no ha alcanzado el límite, la IA genera la etiqueta
        String etiquetaMagica = geminiService.generarEtiqueta(
                nuevoError.getTitulo(),
                nuevoError.getDescripcionContexto(),
                nuevoError.getSolucionAplicada()
        );
        nuevoError.setEtiqueta(etiquetaMagica);
        nuevoError.setUsuarioId(usuario.getId());

        // 3. Guardamos con éxito
        ErrorLog guardado = repository.save(nuevoError);
        return ResponseEntity.ok(guardado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrarError(@PathVariable String id) {
        repository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}