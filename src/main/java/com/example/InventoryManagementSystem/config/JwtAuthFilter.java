package com.example.InventoryManagementSystem.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Phase 19 — previously nothing validated the Authorization header at all; the security
 * chain was `anyRequest().permitAll()` with no JWT filter registered, so every endpoint
 * was reachable with no token whatsoever. This is the first thing that actually checks it.
 *
 * A missing/invalid/expired token just leaves the request unauthenticated (never throws
 * here) — `SecurityConfig`'s authorizeHttpRequests rules decide whether that's a 401.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (jwtUtil.isTokenValid(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                String email = jwtUtil.extractEmail(token);
                String role = jwtUtil.extractRole(token);

                // A token minted before a role was assigned (or for a user whose role
                // was later deleted) still authenticates — just with no authorities, so
                // it satisfies authenticated() but never hasRole(...).
                List<SimpleGrantedAuthority> authorities = role != null
                        ? List.of(new SimpleGrantedAuthority("ROLE_" + role))
                        : List.of();

                var authentication = new UsernamePasswordAuthenticationToken(email, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
