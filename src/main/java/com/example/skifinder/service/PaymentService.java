package com.example.skifinder.service;

import com.example.skifinder.model.Booking;
import com.example.skifinder.repository.BookingRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PaymentService {

    private final BookingRepository bookingRepository;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.webhook.secret}")
    private String stripeWebhookSecret;

    public PaymentService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    /**
     * Crea una sessione di pagamento su Stripe per una prenotazione.
     */
    public String createPaymentSession(Long bookingId) throws StripeException {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            throw new IllegalArgumentException("Prenotazione non trovata");
        }

        Booking booking = bookingOpt.get();
        long amount = (long) (booking.getEquipment().getPrice() * 100); // Stripe lavora con centesimi

        Stripe.apiKey = stripeSecretKey;

        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:3000/payment-success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl("http://localhost:3000/payment-failed")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("eur")
                                                .setUnitAmount(amount)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Noleggio " + booking.getEquipment().getName())
                                                                .build())
                                                .build())
                                .build())
                .build();

        Session session = Session.create(params);
        return session.getId();
    }

    /**
     * Gestisce i webhook di Stripe e aggiorna lo stato del pagamento.
     */
    public void handleStripeWebhook(String payload, String sigHeader) {
        try {
            Stripe.apiKey = stripeSecretKey;
            String endpointSecret = stripeWebhookSecret;

            // Verifica l'autenticità dell'evento ricevuto da Stripe
            Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);

            if ("checkout.session.completed".equals(event.getType())) {
                // Ottieni l'oggetto sessione di pagamento completata
                EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
                Session session = (Session) dataObjectDeserializer.getObject().orElseThrow();

                // Estrarre l'ID della prenotazione dal metadata (Assumendo che tu lo abbia
                // salvato in `metadata`)
                String bookingIdStr = session.getMetadata().get("bookingId");
                if (bookingIdStr != null) {
                    Long bookingId = Long.parseLong(bookingIdStr);

                    // Trova la prenotazione associata al pagamento
                    Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
                    if (bookingOpt.isPresent()) {
                        Booking booking = bookingOpt.get();
                        booking.setPaid(true); // ✅ Imposta la prenotazione come pagata
                        bookingRepository.save(booking); // ✅ Salva la prenotazione aggiornata
                        System.out.println("✅ Prenotazione #" + bookingId + " segnata come pagata!");
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Errore nel webhook: " + e.getMessage());
        }
    }

}
