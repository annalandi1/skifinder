package com.example.skifinder.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class JwtSecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public JwtSecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /**
     * Configura la catena di filtri di sicurezza di Spring Security.
     * <p>
     * Disabilita la protezione CSRF e configura le autorizzazioni per le richieste.
     * <ul>
     * <li>Permette l'accesso alle API di autenticazione (/auth/**)</li>
     * <li>Richiede l'autenticazione per gli endpoint API (/api/users/**)</li>
     * <li>Richiede l'autenticazione per tutte le altre richieste</li>
     * </ul>
     * Inoltre, disabilita la creazione di sessioni e inserisce il filtro
     * {@link JwtAuthFilter} nella catena di filtri.
     * 
     * @return la catena di filtri di sicurezza configurata
     * @throws Exception in caso di errore durante la configurazione
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll() // Permettiamo l'accesso alle API di autenticazione
                        .requestMatchers("/api/users/**").authenticated() // Proteggiamo gli endpoint API
                        .requestMatchers("/api/equipment/**").hasRole("NOLEGGIATORE") // Solo i NOLEGGIATORI possono
                                                                                      // creare un EQUIPMENT
                        .anyRequest().authenticated())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
