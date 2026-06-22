package com.cirio.primelog;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConfig {

    // Aquí le clavamos la URL a la fuerza
    private static final String URI = "mongodb+srv://admin:Katowice2026@primelogcluster.cc6le1x.mongodb.net/primelog?appName=PrimeLogCluster";

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(URI);
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), "primelog");
    }
}