package com.example.skifinder.service;

import com.example.skifinder.model.Equipment;
import com.example.skifinder.model.Location;
import com.example.skifinder.repository.EquipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EquipmentService {

    @Autowired
    private EquipmentRepository equipmentRepository;

    public List<Equipment> getAllEquipment() {
        return equipmentRepository.findAll();
    }

    public Optional<Equipment> getEquipmentById(Long id) {
        return equipmentRepository.findById(id);
    }

    public Equipment addEquipment(Equipment equipment) {
        // Imposta automaticamente il formato della taglia in base al tipo
        equipment.setSize(formatSizeByType(equipment.getType(), equipment.getSize()));
        return equipmentRepository.save(equipment);
    }

    public Equipment updateEquipment(Long id, Equipment updatedEquipment) {
        return equipmentRepository.findById(id)
                .map(equipment -> {
                    equipment.setName(updatedEquipment.getName());
                    equipment.setDescription(updatedEquipment.getDescription());
                    equipment.setPrice(updatedEquipment.getPrice());
                    equipment.setSize(formatSizeByType(updatedEquipment.getType(), updatedEquipment.getSize()));
                    equipment.setType(updatedEquipment.getType());
                    equipment.setLocation(updatedEquipment.getLocation());
                    return equipmentRepository.save(equipment);
                })
                .orElseThrow(() -> new RuntimeException("Equipment not found with id " + id));
    }

    public void deleteEquipment(Long id) {
        equipmentRepository.deleteById(id);
    }

    public List<Equipment> findNearbyEquipment(double lat, double lon, double radius) {
        return equipmentRepository.findAll().stream()
                .filter(equipment -> {
                    Location location = equipment.getLocation();
                    if (location == null) {
                        return false;
                    }
                    double distance = calculateDistance(lat, lon, location.getLatitude(), location.getLongitude());
                    return distance <= radius;
                })
                .collect(Collectors.toList());
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Raggio della Terra in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private String formatSizeByType(String type, String size) {
        if (type == null || size == null) {
            return size;
        }
        switch (type.toLowerCase()) {
            case "sci", "sci alpinismo", "snowboard":
                return size + " cm";
            case "scarponi sci", "scarponi sci alpinismo", "ciaspole":
                return "EU " + size;
            case "casco":
                return switch (size.toLowerCase()) {
                    case "xs", "s", "m", "l", "xl" -> size.toUpperCase();
                    default -> throw new IllegalArgumentException("Taglia non valida per il casco: " + size);
                };
            case "slitta", "bob":
                return "N/A";
            default:
                throw new IllegalArgumentException("Tipo di attrezzatura non riconosciuto: " + type);
        }
    }
}