package com.example.skifinder.service;

import com.example.skifinder.model.Location;
import com.example.skifinder.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationService {

    @Autowired
    private LocationRepository locationRepository;

    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    public Location addLocation(Location location) {
        return locationRepository.save(location);
    }

    public Location getLocationById(Long id) {
        return locationRepository.findById(id).orElse(null);
    }

    public Location updateLocation(Long id, Location location) {

        throw new UnsupportedOperationException("Unimplemented method 'updateLocation'");
    }

    public void deleteLocation(Long id) {

        throw new UnsupportedOperationException("Unimplemented method 'deleteLocation'");
    }
}
