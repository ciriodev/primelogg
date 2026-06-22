package com.cirio.primelog.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;

@Document(collection = "waitlist")
public class WaitlistEntry {

    @Id
    private String id;

    @Indexed(unique = true) // evita duplicados del mismo email
    private String email;

    private Instant createdAt = Instant.now();

    public WaitlistEntry() {}

    public WaitlistEntry(String email) {
        this.email = email;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
