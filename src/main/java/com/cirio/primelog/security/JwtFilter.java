package com.cirio.primelog.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Buscamos la cabecera "Authorization" en la petición
        final String authHeader = request.getHeader("Authorization");
        final String email;
        final String jwt;

        // 2. Si no hay token o no empieza por "Bearer ", le dejamos seguir
        // (Spring Security lo bloqueará más adelante si la ruta era privada)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraemos el token (quitando la palabra "Bearer ")
        jwt = authHeader.substring(7);

        try {
            // 4. Extraemos el email del token
            email = jwtService.extraerEmail(jwt);

            // 5. Si el token es válido, le decimos a Spring que este usuario tiene permiso para entrar
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        email, null, new ArrayList<>()
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            // Si el token está caducado o es falso, fallará silenciosamente y Spring bloqueará el paso
        }

        filterChain.doFilter(request, response);
    }
}