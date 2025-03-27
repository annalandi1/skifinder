package com.example.skifinder.configuration;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        // Utilizza Map per passare le credenziali
        Map<String, String> config = Map.of(
                "cloud_name", "dhzhrqina", // Sostituisci con il tuo cloud name
                "api_key", "512294682663397", // Sostituisci con la tua API key
                "api_secret", "mpve7gTxoypEdZPGb6jP53r2ZRs" // Sostituisci con il tuo API secret
        );

        return new Cloudinary(config);
    }
}
