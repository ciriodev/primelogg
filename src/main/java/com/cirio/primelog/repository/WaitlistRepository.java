package com.cirio.primelog.repository;

import com.cirio.primelog.model.WaitlistEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WaitlistRepository extends MongoRepository<WaitlistEntry, String> {
    Optional<WaitlistEntry> findByEmail(String email);
    boolean existsByEmail(String email);
}
