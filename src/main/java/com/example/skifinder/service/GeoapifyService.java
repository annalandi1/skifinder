package com.example.skifinder.service;

import com.example.skifinder.model.Location;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class GeoapifyService {

    @Value("${geoapify.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public Location geocode(String address) {
        System.out.println("📍 Tentativo di geocodifica per: " + address);
        Location location = attemptGeocode(address);

        if (location != null)
            return location;

        // 🔁 Fallback: rimuove la parte tedesca tipo " - Bozen" o " - Fagenstraße"
        String fallbackAddress = address.replaceAll(" - [^,]+", "");
        System.out.println("⚠️ Primo tentativo fallito, provo fallback con: " + fallbackAddress);

        location = attemptGeocode(fallbackAddress);

        if (location != null)
            return location;

        // 🔁 Fallback 2: prendo solo città e CAP
        String extremeFallback = extractCityAndCapOnly(address);
        System.out.println("⛑️ Ultimo fallback con solo città e CAP: " + extremeFallback);

        location = attemptGeocode(extremeFallback);

        if (location == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Nessun risultato trovato per l'indirizzo (anche con fallback): " + address);
        }

        return location;
    }

    private Location attemptGeocode(String address) {
        try {
            String json = restTemplate.getForObject(buildUrl(address), String.class);
            return parseGeocodeResponse(json, address);
        } catch (Exception e) {
            System.out.println("❌ Errore durante il tentativo di geocodifica: " + e.getMessage());
            return null;
        }
    }

    private String buildUrl(String address) {
        String encodedAddress = UriUtils.encode(address, StandardCharsets.UTF_8);
        return "https://api.geoapify.com/v1/geocode/search?text=" + encodedAddress + "&apiKey=" + apiKey;
    }

    private String extractCityAndCapOnly(String address) {
        String[] parts = address.split(",");
        for (String part : parts) {
            if (part.matches(".*\\d{5}.*")) {
                return part.trim();
            }
        }
        return address;
    }

    public String reverseGeocode(double lat, double lon) {
        try {
            String url = UriComponentsBuilder.fromUriString("https://api.geoapify.com/v1/geocode/reverse")
                    .queryParam("lat", lat)
                    .queryParam("lon", lon)
                    .queryParam("apiKey", apiKey)
                    .toUriString();

            String reverseGeocodeResult = restTemplate.getForObject(url, String.class);
            return parseReverseGeocodeResponse(reverseGeocodeResult);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Errore durante il reverse geocoding",
                    e);
        }
    }

    public List<String> autocompleteAddress(String query) {
        try {
            String url = UriComponentsBuilder.fromUriString("https://api.geoapify.com/v1/geocode/autocomplete")
                    .queryParam("text", query)
                    .queryParam("apiKey", apiKey)
                    .toUriString();

            String autocompleteResult = restTemplate.getForObject(url, String.class);
            return parseAutocompleteResponse(autocompleteResult);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Errore durante l'autocompletamento",
                    e);
        }
    }

    private Location parseGeocodeResponse(String jsonResponse, String address) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode featuresArray = rootNode.path("features");

            if (!featuresArray.isArray() || featuresArray.size() == 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Nessun risultato trovato per l'indirizzo: " + address);
            }

            JsonNode featuresNode = featuresArray.get(0).path("properties");

            double lat = featuresNode.path("lat").asDouble();
            double lon = featuresNode.path("lon").asDouble();

            Location location = new Location();
            location.setName(address);
            location.setLatitude(lat);
            location.setLongitude(lon);

            return location;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Errore nel parsing della geocodifica",
                    e);
        }
    }

    private String parseReverseGeocodeResponse(String jsonResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode featuresNode = rootNode.path("features").get(0).path("properties");
            return featuresNode.path("formatted").asText();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Errore nel parsing del reverse geocoding", e);
        }
    }

    private List<String> parseAutocompleteResponse(String jsonResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode featuresNode = rootNode.path("features");

            List<String> suggestions = new ArrayList<>();
            for (JsonNode feature : featuresNode) {
                String address = feature.path("properties").path("formatted").asText();
                suggestions.add(address);
            }

            return suggestions;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Errore nel parsing dell'autocomplete",
                    e);
        }
    }
}
