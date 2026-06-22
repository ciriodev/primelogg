package com.cirio.primelog.controller;

import com.cirio.primelog.model.User;
import com.cirio.primelog.repository.UserRepository;
import com.cirio.primelog.security.JwtService;
import com.stripe.Stripe;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/pagos")
@CrossOrigin(origins = "*")
public class SubscriptionController {

    @Value("${stripe.api.key}")
    private String stripeKey;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public SubscriptionController(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    // ==========================================
    // RUTA 1: CREAR SESIÓN DE PAGO
    // Recibe el JWT en la cabecera Authorization para saber quién es el usuario
    // ==========================================
    @PostMapping("/crear-sesion")
    public ResponseEntity<Map<String, String>> crearSesion(
            @RequestHeader("Authorization") String authHeader) throws Exception {

        // 1. Extraemos el email del token JWT para buscar al usuario
        String token = authHeader.replace("Bearer ", "").trim();
        String email = jwtService.extraerEmail(token);

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Usuario no encontrado"));
        }

        User user = userOpt.get();
        String userId = user.getId(); // ID de MongoDB, p.ej: "6842a1f3c9e77b001e2d3f91"

        // 2. Inicializamos Stripe
        Stripe.apiKey = stripeKey;

        // 3. Creamos la sesión con client_reference_id = userId de MongoDB
        //    Esto es el puente entre Stripe y nuestra base de datos
        SessionCreateParams params = SessionCreateParams.builder()
                // ✅ CLAVE: pasamos el ID de MongoDB, no el email
                .setClientReferenceId(userId)
                // Opcional: pre-rellena el email en el formulario de Stripe (mejor UX)
                .setCustomerEmail(user.getEmail())
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSuccessUrl("http://127.0.0.1:3000/success.html?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl("http://127.0.0.1:3000/index.html")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("eur")
                                                .setUnitAmount(499L) // 4.99€
                                                .setRecurring(
                                                        SessionCreateParams.LineItem.PriceData.Recurring.builder()
                                                                .setInterval(SessionCreateParams.LineItem.PriceData.Recurring.Interval.MONTH)
                                                                .build()
                                                )
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("PrimeLog PRO")
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        Session session = Session.create(params);

        System.out.println("✅ Sesión de pago creada para userId=" + userId + " | sessionId=" + session.getId());

        Map<String, String> response = new HashMap<>();
        response.put("url", session.getUrl());
        return ResponseEntity.ok(response);
    }

    // ==========================================
    // RUTA 2: WEBHOOK — Stripe nos avisa del pago
    // Stripe llama a este endpoint automáticamente tras el pago
    // NO requiere JWT: la seguridad viene de verificar la firma de Stripe
    // ==========================================
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        // 1. Verificamos que la llamada viene realmente de Stripe (firma HMAC)
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret.trim());
        } catch (SignatureVerificationException e) {
            System.out.println("🚨 Firma de Stripe inválida. Posible intento de fraude.");
            return ResponseEntity.status(400).body("Firma inválida");
        } catch (Exception e) {
            System.out.println("🚨 Error procesando el webhook: " + e.getMessage());
            return ResponseEntity.status(400).body("Error en el payload");
        }

        // 2. Gestionamos los eventos que nos interesan
        switch (event.getType()) {

            case "checkout.session.completed" -> {
                // El usuario ha pagado con éxito
                Session session;
                try {
                    session = (Session) event.getDataObjectDeserializer()
                            .getObject()
                            .orElseGet(() -> {
                                try {
                                    return event.getDataObjectDeserializer().deserializeUnsafe();
                                } catch (EventDataObjectDeserializationException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                } catch (Exception e) {
                    System.out.println("❌ No se pudo deserializar la sesión: " + e.getMessage());
                    return ResponseEntity.ok("Error de deserialización, ignorado");
                }

                // ✅ Leemos el userId de MongoDB que pusimos al crear la sesión
                String userId = session.getClientReferenceId();

                if (userId == null || userId.isBlank()) {
                    System.out.println("⚠️ checkout.session.completed sin client_reference_id. SessionId=" + session.getId());
                    return ResponseEntity.ok("Sin client_reference_id");
                }

                // Buscamos al usuario por su ID de MongoDB
                Optional<User> userOpt = userRepository.findById(userId);

                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    user.setRole("PRO");
                    // Guardamos también el ID de cliente Stripe (útil para gestionar bajas)
                    user.setStripeCustomerId(session.getCustomer());
                    userRepository.save(user);
                    System.out.println("🚀 ¡PRO activado! userId=" + userId + " | email=" + user.getEmail());
                } else {
                    System.out.println("⚠️ Pagó, pero no encontré ningún usuario con id=" + userId + " en MongoDB.");
                }
            }

            case "customer.subscription.deleted" -> {
                // El usuario ha cancelado o ha expirado su suscripción
                com.stripe.model.Subscription subscription;
                try {
                    subscription = (com.stripe.model.Subscription) event.getDataObjectDeserializer()
                            .getObject()
                            .orElseGet(() -> {
                                try {
                                    return event.getDataObjectDeserializer().deserializeUnsafe();
                                } catch (EventDataObjectDeserializationException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                } catch (Exception e) {
                    return ResponseEntity.ok("Error de deserialización en cancelación");
                }

                String stripeCustomerId = subscription.getCustomer();
                System.out.println("🔔 Suscripción cancelada para stripeCustomerId=" + stripeCustomerId);

                // Buscamos por el stripeCustomerId que guardamos al activar
                Optional<User> userOpt = userRepository.findByStripeCustomerId(stripeCustomerId);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    user.setRole("FREE");
                    userRepository.save(user);
                    System.out.println("↩️ Rol revertido a FREE para userId=" + user.getId());
                } else {
                    System.out.println("⚠️ Cancelación recibida pero no encontré usuario con stripeCustomerId=" + stripeCustomerId);
                }
            }

            default -> System.out.println("ℹ️ Evento de Stripe ignorado: " + event.getType());
        }

        // Stripe requiere un 200 rápido para confirmar recepción
        return ResponseEntity.ok("OK");
    }
}