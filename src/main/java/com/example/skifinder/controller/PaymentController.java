package com.example.skifinder.controller;

import com.example.skifinder.service.PaymentService;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * Crea una sessione di pagamento su Stripe
     */
    @PostMapping("/create-session")
    public ResponseEntity<Map<String, String>> createPaymentSession(@RequestParam Long bookingId) {
        try {
            String sessionId = paymentService.createPaymentSession(bookingId);
            return ResponseEntity.ok(Map.of("sessionId", sessionId));
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint webhook per gestire gli eventi di pagamento da Stripe
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            paymentService.handleStripeWebhook(payload, sigHeader);
            return ResponseEntity.ok("Webhook elaborato correttamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Errore nella gestione del webhook: " + e.getMessage());
        }
    }
}
