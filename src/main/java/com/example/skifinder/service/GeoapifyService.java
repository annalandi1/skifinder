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

import java.net.URI;

import java.util.ArrayList;
import java.util.List;

@Service
public class GeoapifyService {

    @Value("${geoapify.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public Location geocode(String indirizzo) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString("https://api.geoapify.com/v1/geocode/search")
                    .queryParam("text", indirizzo)
                    .queryParam("lang", "it")
                    .queryParam("limit", 1)
                    .queryParam("apiKey", apiKey);

            URI uri = builder.build().toUri();

            String json = restTemplate.getForObject(uri, String.class);
            System.out.println("📦 Risposta Geoapify:\n" + json); // Debug

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);
            JsonNode features = root.path("features");

            if (features.isArray() && features.size() > 0) {
                JsonNode geometry = features.get(0).path("geometry");
                JsonNode properties = features.get(0).path("properties");

                double lat = geometry.path("coordinates").get(1).asDouble();
                double lon = geometry.path("coordinates").get(0).asDouble();
                String name = properties.path("city").asText(); // es: "Merano"
                String address = properties.path("formatted").asText(); // es: "Via Roma 5, 39012 Merano"

                return new Location(name, address, lat, lon);
            }

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Nessun risultato trovato per l'indirizzo: " + indirizzo);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Errore nel parsing della geocodifica", e);
        }
    }

    public String reverseGeocode(double lat, double lon) {
        try {
            String url = UriComponentsBuilder.fromUriString("https://api.geoapify.com/v1/geocode/reverse")
                    .queryParam("lat", lat)
                    .queryParam("lon", lon)
                    .queryParam("lang", "it")
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
                    .queryParam("lang", "it")
                    .queryParam("limit", 5)
                    .queryParam("apiKey", apiKey)
                    .toUriString();

            String autocompleteResult = restTemplate.getForObject(url, String.class);
            return parseAutocompleteResponse(autocompleteResult);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Errore durante l'autocompletamento",
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
