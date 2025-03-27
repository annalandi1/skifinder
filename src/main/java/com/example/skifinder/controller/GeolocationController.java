package com.example.skifinder.controller;

import com.example.skifinder.model.Location;
import com.example.skifinder.service.GeoapifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/geolocation")
public class GeolocationController {

    @Autowired
    private GeoapifyService geoapifyService;

    @GetMapping("/geocode")
    public Location geocode(@RequestParam String address) {
        return geoapifyService.geocode(address);
    }

    @GetMapping("/reverse-geocode")
    public String reverseGeocode(@RequestParam double lat, @RequestParam double lon) {
        return geoapifyService.reverseGeocode(lat, lon);
    }

    @GetMapping("/autocomplete")
    public List<String> autocomplete(@RequestParam String query) {
        return geoapifyService.autocompleteAddress(query);
    }
}
