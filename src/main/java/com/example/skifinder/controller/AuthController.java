package com.example.skifinder.controller;

import com.example.skifinder.model.Role;
import com.example.skifinder.model.User;
import com.example.skifinder.repository.UserRepository;
import com.example.skifinder.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Classe per la richiesta di login
    static class LoginRequest {
        private String login; // username, email o telefono
        private String password; // password in chiaro

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    /**
     * 1) REGISTRAZIONE
     * - Salva l'utente con password criptata
     * - Ritorna un token JWT (logga subito l'utente)
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody User user) {
        // 1. Verifica password
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password cannot be null or empty"));
        }
        // 2. Cripta password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // 3. Imposta ruolo di default se manca
        if (user.getRole() == null) {
            user.setRole(Role.CLIENTE);
        }
        // 4. Salva nel DB
        userRepository.save(user);

        // 5. Genera token (come prima)
        String token = jwtService.generateToken(
                org.springframework.security.core.userdetails.User.builder()
                        .username(getPreferredLogin(user))
                        .password(user.getPassword()) // hashed
                        .roles(user.getRole().name())
                        .build());

        return ResponseEntity.ok(Map.of("token", token));
    }

    /**
     * 2) LOGIN
     * - Riceve { "login": "...", "password": "..." }
     * - Permette username / email / phone
     * - Ritorna un token JWT
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest req) {
        // 1. Autentichiamo con login e password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        req.getLogin(),
                        req.getPassword()));

        // 2. Recuperiamo l'utente dal DB per generare il token
        Optional<User> userOpt = findUserByAnyField(req.getLogin());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found for " + req.getLogin()));
        }
        User user = userOpt.get();

        // 3. Genera token JWT
        String token = jwtService.generateToken(
                org.springframework.security.core.userdetails.User.builder()
                        .username(getPreferredLogin(user))
                        .password(user.getPassword()) // hashed
                        .roles(user.getRole().name())
                        .build());

        return ResponseEntity.ok(Map.of("token", token));
    }

    /**
     * Cerca l'utente su username/email/phoneNumber
     */
    private Optional<User> findUserByAnyField(String login) {
        Optional<User> u = userRepository.findByUsername(login);
        if (u.isPresent())
            return u;

        u = userRepository.findByEmail(login);
        if (u.isPresent())
            return u;

        return userRepository.findByPhoneNumber(login);
    }

    /**
     * Scegli come "subject" del token: username, email o phone
     */
    private String getPreferredLogin(User user) {
        if (user.getUsername() != null && !user.getUsername().isEmpty()) {
            return user.getUsername();
        }
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            return user.getEmail();
        }
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
            return user.getPhoneNumber();
        }
        return "User#" + user.getId();
    }
}
