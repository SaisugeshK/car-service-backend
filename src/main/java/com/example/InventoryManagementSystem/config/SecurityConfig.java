package com.example.InventoryManagementSystem.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .exceptionHandling(eh -> eh.authenticationEntryPoint(
                        (req, res, ex) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        // The browser's CORS preflight (OPTIONS) never carries the Authorization
                        // header, so it must be let through before the real request is even sent —
                        // otherwise every authenticated endpoint is unreachable cross-origin: the
                        // preflight itself gets rejected before Spring MVC's CORS handling can
                        // attach the Access-Control-* headers, and the browser blocks the real
                        // request without it ever reaching the server. (CorsConfig's WebMvcConfigurer
                        // mapping alone doesn't help here — that only runs after Spring Security.)
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        // Safe, customer-facing branding only (company name/logo/tagline/phone) —
                        // reachable before login, since the login page itself needs to render it.
                        // The controller behind this path decides exactly which settings keys are
                        // exposed here; it never forwards the full settings table.
                        .requestMatchers("/api/public/**").permitAll()
                        // Reading the staff list and business settings isn't owner-only information —
                        // any authenticated role needs it (technician/delivery staff-assignment
                        // dropdowns, invoice/receipt company branding on a print/PDF). Only mutating
                        // them (create/edit/delete a user, edit settings) stays SUPER_ADMIN-only,
                        // via the general rule below matching every method once GET is spoken for.
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/users/**", "/api/settings/**").authenticated()
                        // Explicitly SUPER_ADMIN-only per the ERP spec: Users, Roles, Settings, Audit Log (Phase 30).
                        .requestMatchers("/api/users/**", "/api/roles/**", "/api/settings/**", "/api/audit-logs/**").hasRole("SUPER_ADMIN")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Same origins/methods/headers as CorsConfig (MVC-level) — duplicated here because Spring
    // Security's own filter chain runs before Spring MVC and does not see WebMvcConfigurer's
    // CORS mappings; it needs its own CorsConfigurationSource to answer preflight requests.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
