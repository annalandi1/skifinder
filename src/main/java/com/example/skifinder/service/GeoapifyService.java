package com.example.skifinder.service;

import com.example.skifinder.model.Location;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class GeoapifyService {

    @Value("${geoapify.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    // Geocodifica un indirizzo e restituisce un oggetto Location con le coordinate.

    public Location geocode(String address) throws IOException {
        String url = UriComponentsBuilder.fromUriString("https://api.geoapify.com/v1/geocode/search")
                .queryParam("text", address)
                .queryParam("apiKey", apiKey)
                .toUriString();

        String geocodeResult = restTemplate.getForObject(url, String.class);
        return parseGeocodeResponse(geocodeResult, address);
    }

    // Reverse geocoding: converte coordinate (lat, lon) in un indirizzo.
    public String reverseGeocode(double lat, double lon) throws IOException {
        String url = UriComponentsBuilder.fromUriString("https://api.geoapify.com/v1/geocode/reverse")
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("apiKey", apiKey)
                .toUriString();

        String reverseGeocodeResult = restTemplate.getForObject(url, String.class);
        return parseReverseGeocodeResponse(reverseGeocodeResult);
    }

    // Autocompletamento: suggerisce indirizzi in base a una query.
    public List<String> autocompleteAddress(String query) throws IOException {
        String url = UriComponentsBuilder.fromUriString("https://api.geoapify.com/v1/geocode/autocomplete")
                .queryParam("text", query)
                .queryParam("apiKey", apiKey)
                .toUriString();

        String autocompleteResult = restTemplate.getForObject(url, String.class);
        return parseAutocompleteResponse(autocompleteResult);
    }

    // Parsa la risposta JSON di Geoapify per la geocodifica.
    private Location parseGeocodeResponse(String jsonResponse, String address) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        JsonNode featuresNode = rootNode.path("features").get(0).path("properties");

        double lat = featuresNode.path("lat").asDouble();
        double lon = featuresNode.path("lon").asDouble();

        Location location = new Location();
        location.setName(address); // Imposta l'indirizzo inserito dall'utente
        location.setLatitude(lat); // Imposta la latitudine
        location.setLongitude(lon); // Imposta la longitudine

        return location;
    }

    // Parsa la risposta JSON di Geoapify per il reverse geocoding.
    private String parseReverseGeocodeResponse(String jsonResponse) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        JsonNode featuresNode = rootNode.path("features").get(0).path("properties");

        return featuresNode.path("formatted").asText();
    }

    // Parsa la risposta JSON di Geoapify per l'autocompletamento.
    private List<String> parseAutocompleteResponse(String jsonResponse) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        JsonNode featuresNode = rootNode.path("features");

        List<String> suggestions = new ArrayList<>();
        for (JsonNode feature : featuresNode) {
            String address = feature.path("properties").path("formatted").asText();
            suggestions.add(address);
        }

        return suggestions;
    }
}