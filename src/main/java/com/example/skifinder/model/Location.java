package com.example.skifinder.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // Nome della località
    private double latitude; // Latitudine
    private double longitude; // Longitudine

    private String address;

    public Location() {
    }

    // Costruttore per creare una Location con indirizzo
    public Location(String address) {
        this.address = address;
    }

    public Location(String name, String address, double latitude, double longitude) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
