package com.example.skifinder.controller;

import com.cloudinary.Cloudinary;
import com.example.skifinder.model.Role;
import com.example.skifinder.model.User;
import com.example.skifinder.repository.UserRepository;
import com.example.skifinder.security.JwtService;
import com.example.skifinder.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserService userService;
    @Autowired
    private Cloudinary cloudinary; // Iniettato Cloudinary

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
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody User user) {
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password cannot be null or empty"));
        }
        if (userRepository.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username già esistente"));
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email già registrata"));
        }
        if (userRepository.existsByPhoneNumber(user.getPhoneNumber())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Numero di telefono già registrato"));
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null) {
            user.setRole(Role.CLIENTE);
        }

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Errore nel salvataggio dell'utente"));
        }

        String token = jwtService.generateToken(
                org.springframework.security.core.userdetails.User.builder()
                        .username(getPreferredLogin(user))
                        .password(user.getPassword())
                        .roles(user.getRole().name())
                        .build());

        return ResponseEntity.ok(Map.of("token", token));
    }

    /**
     * 2) LOGIN
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getLogin(), req.getPassword()));

        Optional<User> userOpt = findUserByAnyField(req.getLogin());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found for " + req.getLogin()));
        }

        User user = userOpt.get();

        String token = jwtService.generateToken(
                org.springframework.security.core.userdetails.User.builder()
                        .username(getPreferredLogin(user))
                        .password(user.getPassword())
                        .roles(user.getRole().name())
                        .build());

        return ResponseEntity.ok(Map.of("token", token));
    }

    /**
     * 3) Ottieni profilo dell'utente attualmente loggato
     */
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByLogin(userDetails.getUsername());
        return ResponseEntity.ok(currentUser);
    }

    /**
     * 4) Modifica profilo dell'utente loggato
     */
    @PutMapping("/me")
    public ResponseEntity<User> updateProfile(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody User updatedUser) {
        User user = userService.updateCurrentUser(userDetails.getUsername(), updatedUser);
        return ResponseEntity.ok(user);
    }

    /**
     * 5) Carica e aggiorna la foto del profilo dell'utente
     */
    @PutMapping("/me/photo")
    public ResponseEntity<?> uploadProfilePicture(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        try {
            System.out.println("📸 Upload richiesto da: " + userDetails.getUsername());

            // Carica l'immagine su Cloudinary
            Map<String, Object> uploadParams = new HashMap<>();
            uploadParams.put("resource_type", "auto");
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = (Map<String, Object>) cloudinary.uploader().upload(file.getBytes(),
                    uploadParams);
            // Otteniamo l'URL sicuro dell'immagine
            String photoUrl = (String) uploadResult.get("secure_url");

            // Aggiorna nel DB l'URL della foto
            userService.updatePhoto(userDetails.getUsername(), photoUrl);

            return ResponseEntity.ok().body(Map.of("url", photoUrl));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore upload immagine profilo");
        }
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
     * Scegli come "subject" del token
     */
    private String getPreferredLogin(User user) {
        if (user.getUsername() != null && !user.getUsername().isEmpty())
            return user.getUsername();
        if (user.getEmail() != null && !user.getEmail().isEmpty())
            return user.getEmail();
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty())
            return user.getPhoneNumber();
        return "User#" + user.getId();
    }
}
