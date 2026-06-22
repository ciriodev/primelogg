package com.cirio.primelog.controller;

import com.cirio.primelog.model.User;
import com.cirio.primelog.repository.UserRepository;
import com.cirio.primelog.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    // Nuestro encriptador de contraseñas
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    // 1. RUTA DE REGISTRO
    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@RequestBody User newUser) {
        // Comprobamos si el correo ya existe
        if (userRepository.findByEmail(newUser.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("El email ya está registrado en PrimeLog");
        }

        // Encriptamos la contraseña y guardamos el usuario en MongoDB
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        userRepository.save(newUser);

        return ResponseEntity.ok("Usuario registrado con éxito");
    }

    // 2. RUTA DE LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        // Buscamos al usuario por su email
        Optional<User> userOpt = userRepository.findByEmail(loginRequest.getEmail());

        // Si existe y la contraseña introducida coincide con la encriptada...
        if (userOpt.isPresent() && passwordEncoder.matches(loginRequest.getPassword(), userOpt.get().getPassword())) {

            // ...¡Fabricamos la pulsera VIP (Token)!
            String token = jwtService.generarToken(loginRequest.getEmail());

            // Se la enviamos al frontend
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("email", loginRequest.getEmail());
            return ResponseEntity.ok(response);
        }

        // Si falla, le damos un portazo
        return ResponseEntity.status(401).body("Credenciales incorrectas");
    }
}