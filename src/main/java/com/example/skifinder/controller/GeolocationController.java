package com.example.skifinder.controller;

import com.example.skifinder.model.Location;
import com.example.skifinder.service.GeoapifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/geolocation")
public class GeolocationController {

    @Autowired
    private GeoapifyService geoapifyService;

    @GetMapping("/geocode")
    public Location geocode(@RequestParam String address) {
        try {
            return geoapifyService.geocode(address);
        } catch (IOException e) {
            throw new RuntimeException("Failed to geocode address", e);
        }
    }

    @GetMapping("/reverse-geocode")
    public String reverseGeocode(@RequestParam double lat, @RequestParam double lon) {
        try {
            return geoapifyService.reverseGeocode(lat, lon);
        } catch (IOException e) {
            throw new RuntimeException("Failed to reverse geocode coordinates", e);
        }
    }
}