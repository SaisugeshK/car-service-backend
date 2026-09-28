package com.example.InventoryManagementSystem.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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

    private static final String[] BOTH = {"SUPER_ADMIN", "EMPLOYEE"};

    private final JwtAuthFilter jwtAuthFilter;

    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

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
                        // Only a SUPER_ADMIN creates accounts — open self-registration would hand
                        // anyone who finds the URL a login.
                        .requestMatchers("/api/auth/register").hasRole("SUPER_ADMIN")
                        .requestMatchers("/api/auth/**").permitAll()
                        // Pre-deployment fix — Cloud Run's health probe carries no JWT; the
                        // endpoint itself only ever reports up/down (management.endpoint.health.
                        // show-details=never in application.properties), so this is safe to leave
                        // open.
                        .requestMatchers("/actuator/health").permitAll()
                        // Safe, customer-facing branding only (company name/logo/tagline/phone) —
                        // reachable before login, since the login page itself needs to render it.
                        // The controller behind this path decides exactly which settings keys are
                        // exposed here; it never forwards the full settings table.
                        .requestMatchers("/api/public/**").permitAll()
                        // Two roles. SUPER_ADMIN: everything. EMPLOYEE: exactly the paths below —
                        // anything not listed (billing, invoices, payments, offers, CRM, payroll
                        // runs, salary config, reports, users, settings, ...) falls through to the
                        // SUPER_ADMIN-only rule at the end.
                        //
                        // Own records only — the user is taken from the JWT (the /my endpoints), and
                        // a payslip fetched by id is ownership-checked in PayrollServiceImpl.
                        .requestMatchers(HttpMethod.GET,
                                "/api/payroll/my", "/api/payroll/*", "/api/attendance/my",
                                "/api/leave-requests/my", "/api/overtime/my",
                                "/api/company-details").hasAnyRole(BOTH)
                        // Workshop — only job cards/appointments assigned to them; the services
                        // filter lists and refuse other ids (JobCardAccessService).
                        .requestMatchers(HttpMethod.GET,
                                "/api/job-cards", "/api/job-cards/*", "/api/job-cards/*/status-history",
                                "/api/appointments", "/api/appointments/*",
                                "/api/inspection-items/job-card/*", "/api/inspection-items/*/photos",
                                "/api/inspection-items/photos/*").hasAnyRole(BOTH)
                        // Update an assigned job card: status (workshop states only) and work notes
                        // — JobCardServiceImpl strips every other field for an EMPLOYEE.
                        .requestMatchers(HttpMethod.PUT, "/api/job-cards/*").hasAnyRole(BOTH)
                        .requestMatchers(HttpMethod.PATCH, "/api/job-cards/*/status").hasAnyRole(BOTH)
                        // Read-only reference data: customers, vehicles, catalog, inventory.
                        .requestMatchers(HttpMethod.GET,
                                "/api/customers", "/api/customers/*",
                                "/api/vehicles", "/api/vehicles/**",
                                "/api/services", "/api/services/*",
                                "/api/products", "/api/products/*", "/api/categories", "/api/categories/*",
                                "/api/product-taxes", "/api/product-taxes/*",
                                "/api/stock-movements", "/api/stock-movements/**",
                                "/api/purchases", "/api/purchases/*", "/api/suppliers", "/api/suppliers/*").hasAnyRole(BOTH)
                        // Expenses: an EMPLOYEE records and manages only their own (ExpenseService
                        // scopes every call); a SUPER_ADMIN sees all.
                        .requestMatchers("/api/expenses", "/api/expenses/**").hasAnyRole(BOTH)
                        // Visits: log and read (an EMPLOYEE's full list is only visits they handled);
                        // deleting one stays SUPER_ADMIN (falls through to the rule at the end).
                        .requestMatchers(HttpMethod.GET, "/api/visits", "/api/visits/**").hasAnyRole(BOTH)
                        .requestMatchers(HttpMethod.POST, "/api/visits").hasAnyRole(BOTH)
                        // Complaints: view and log.
                        .requestMatchers(HttpMethod.GET, "/api/complaints", "/api/complaints/*").hasAnyRole(BOTH)
                        .requestMatchers(HttpMethod.POST, "/api/complaints").hasAnyRole(BOTH)
                        .requestMatchers("/api/**").hasRole("SUPER_ADMIN")
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Spring Security's own filter chain runs before Spring MVC and never sees a plain
    // WebMvcConfigurer's CORS mappings, so this is the one CorsConfigurationSource that
    // actually governs preflight/CORS for every request. Origins come from cors.allowed-origins
    // (application.properties), overridable via CORS_ALLOWED_ORIGINS at deploy time.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
