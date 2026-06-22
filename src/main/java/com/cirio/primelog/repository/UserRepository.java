package com.cirio.primelog.repository;

import com.cirio.primelog.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    // Busca por email (login normal)
    Optional<User> findByEmail(String email);

    // ✅ NUEVO: busca por el ID de cliente Stripe
    // Se usa en el webhook de cancelación para revocar el rol PRO
    Optional<User> findByStripeCustomerId(String stripeCustomerId);
}