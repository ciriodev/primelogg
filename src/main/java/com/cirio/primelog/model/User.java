package com.cirio.primelog.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "usuarios")
public class User {

    @Id
    private String id;

    // Usaremos el email como nombre de usuario para el login
    private String email;

    // Aquí guardaremos la contraseña, pero estará encriptada (nunca en texto plano)
    private String password;

    private String role = "FREE";

    // ✅ NUEVOS: IDs de Stripe para gestionar la suscripción
    // Se rellenan automáticamente en el webhook al pagar
    private String stripeCustomerId;      // "cus_Qx..." — para gestionar bajas
    private String stripeSubscriptionId;  // "sub_Qx..." — para consultar estado

    public User() {
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // --- Getters y Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStripeCustomerId() {
        return stripeCustomerId;
    }

    public void setStripeCustomerId(String stripeCustomerId) {
        this.stripeCustomerId = stripeCustomerId;
    }

    public String getStripeSubscriptionId() {
        return stripeSubscriptionId;
    }

    public void setStripeSubscriptionId(String stripeSubscriptionId) {
        this.stripeSubscriptionId = stripeSubscriptionId;
    }
}