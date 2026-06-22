package com.cirio.primelog.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    // ¡NUEVO! Llave maestra estática. Así los tokens no mueren al reiniciar el servidor.
    private static final Key SECRET_KEY = Keys.hmacShaKeyFor("EstaClaveSuperSecretaEsParaElSaaSDePrimeLog2026Marc".getBytes());

    private static final long EXPIRATION_TIME = 86400000;

    public String generarToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    public String extraerEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}