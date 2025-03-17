package com.example.skifinder.controller;

import com.example.skifinder.model.Location;
import com.example.skifinder.service.GeoapifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/geolocation")
public class GeolocationController {

    @Autowired
    private GeoapifyService geoapifyService;

    // Geocodifica un indirizzo e restituisce un oggetto Location con le coordinate.
    @GetMapping("/geocode")
    public Location geocode(@RequestParam String address) {
        try {
            return geoapifyService.geocode(address);
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Errore durante la geocodifica dell'indirizzo: " + e.getMessage(),
                    e);
        }
    }

    // Reverse geocoding: converte coordinate (lat, lon) in un indirizzo.
    @GetMapping("/reverse-geocode")
    public String reverseGeocode(@RequestParam double lat, @RequestParam double lon) {
        try {
            return geoapifyService.reverseGeocode(lat, lon);
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Errore durante il reverse geocoding delle coordinate: " + e.getMessage(),
                    e);
        }
    }

    // Autocompletamento: suggerisce indirizzi in base a una query.
    @GetMapping("/autocomplete")
    public List<String> autocomplete(@RequestParam String query) {
        try {
            return geoapifyService.autocompleteAddress(query);
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Errore durante l'autocompletamento dell'indirizzo: " + e.getMessage(),
                    e);
        }
    }
}