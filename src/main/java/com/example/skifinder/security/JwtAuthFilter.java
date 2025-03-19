package com.example.skifinder.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        System.out.println("🔍 [JwtAuthFilter] Authorization header ricevuto: " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("❌ [JwtAuthFilter] Header Authorization mancante o non valido.");
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);
        System.out.println("🔑 [JwtAuthFilter] Token JWT estratto: " + jwt);

        String username = jwtService.extractUsername(jwt);
        System.out.println("👤 [JwtAuthFilter] Username estratto dal token: " + username);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            System.out.println("🔍 [JwtAuthFilter] Nessuna autenticazione attiva, carico i dettagli utente...");

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            System.out.println("✅ [JwtAuthFilter] UserDetails caricati: " + userDetails.getUsername());
            System.out.println("🔑 [JwtAuthFilter] Ruoli utente: " + userDetails.getAuthorities());

            if (jwtService.validateToken(jwt, userDetails)) {
                System.out.println("✅ [JwtAuthFilter] Token valido, impostazione dell'autenticazione.");
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
                        null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                System.out.println("❌ [JwtAuthFilter] Token non valido.");
            }
        } else {
            if (username == null) {
                System.out.println("❌ [JwtAuthFilter] Username estratto dal token è null.");
            } else {
                System.out.println("⚠️ [JwtAuthFilter] L'utente è già autenticato nel SecurityContext.");
            }
        }

        System.out.println("➡️ [JwtAuthFilter] Passo il controllo al prossimo filtro.");
        filterChain.doFilter(request, response);
    }
}
