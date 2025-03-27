package com.example.skifinder.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import com.stripe.Stripe;

@Configuration
public class StripeConfig {

    private static final Logger logger = LoggerFactory.getLogger(StripeConfig.class);

    private final StripeProperties stripeProperties;

    public StripeConfig(StripeProperties stripeProperties) {
        this.stripeProperties = stripeProperties;
        initStripe();
    }

    // Metodo per inizializzare la chiave Stripe
    public void initStripe() {
        Stripe.apiKey = stripeProperties.getSecretKey();
        if (Stripe.apiKey == null || Stripe.apiKey.isBlank()) {
            logger.error("❌ ERRORE: Stripe API key non configurata!");
        } else {
            logger.info("✅ Stripe API key configurata correttamente.");
        }
    }
}
