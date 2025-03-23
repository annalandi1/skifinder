package com.example.skifinder.controller;

import com.example.skifinder.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<String> sendTestEmail(@RequestParam String to) {
        try {
            emailService.sendEmail(to, "Test Email", "Questo è un messaggio di test.");
            return ResponseEntity.ok("Email inviata con successo!");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante l'invio dell'email: " + e.getMessage());
        }
    }

}
