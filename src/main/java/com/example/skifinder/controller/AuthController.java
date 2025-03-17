package com.example.skifinder.controller;

import com.example.skifinder.model.User;
import com.example.skifinder.repository.UserRepository;
import com.example.skifinder.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

    // Usa JwtService al posto di JwtUtil
    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Classe di supporto per la login
     */
    static class LoginRequest {
        public String login; // potrebbe essere username, email o phone
        public String password;

        public String getLogin() {
            return login;
        }

        public String getPassword() {
            return password;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody User user) {
        // Verifica password non nulla
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password cannot be null or empty"));
        }

        // Cripta la password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Se role è null, metti un default
        if (user.getRole() == null) {
            user.setRole(com.example.skifinder.model.Role.CLIENTE);
        }
        // Salva
        userRepository.save(user);

        // Generiamo un token per l’utente appena registrato
        org.springframework.security.core.userdetails.UserDetails userDetails = org.springframework.security.core.userdetails.User
                .builder()
                .username(getPreferredLogin(user))
                .password(user.getPassword()) // hashed
                .roles(user.getRole().name())
                .build();

        String token = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest req) {
        // Esempio: "login" e "password"
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        req.getLogin(),
                        req.getPassword()));
        // Se tutto va bene, carichiamo dal DB
        Optional<User> userOpt = findUserByAnyField(req.getLogin());
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        User user = userOpt.get();

        // Genera token JWT (usando JwtService)
        String token = jwtService.generateToken(
                org.springframework.security.core.userdetails.User.builder()
                        .username(preferredLogin(user))
                        .password(user.getPassword())
                        .roles(user.getRole().name())
                        .build());
        return Map.of("token", token);
    }

    // Cerca l'utente con 'login' come username, email, o phone
    private Optional<User> findUserByAnyField(String login) {
        Optional<User> u = userRepository.findByUsername(login);
        if (u.isPresent())
            return u;
        u = userRepository.findByEmail(login);
        if (u.isPresent())
            return u;
        return userRepository.findByPhoneNumber(login);
    }

    // Decide come vuoi "etichettare" lo username nel token
    private String getPreferredLogin(User user) {
        if (user.getUsername() != null)
            return user.getUsername();
        if (user.getEmail() != null)
            return user.getEmail();
        if (user.getPhoneNumber() != null)
            return user.getPhoneNumber();
        return "User#" + user.getId();
    }
}
