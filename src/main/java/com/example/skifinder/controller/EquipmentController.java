package com.example.skifinder.controller;

import com.example.skifinder.model.Equipment;
import com.example.skifinder.model.Location;
import com.example.skifinder.service.EquipmentService;
import com.example.skifinder.service.GeoapifyService;
import com.example.skifinder.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    @Autowired
    private EquipmentService equipmentService;

    @Autowired
    private GeoapifyService geoapifyService;

    @Autowired
    private LocationService locationService;

    @GetMapping
    public List<Equipment> getAllEquipment() {
        return equipmentService.getAllEquipment();
    }

    @GetMapping("/{id}")
    public Equipment getEquipmentById(@PathVariable Long id) {
        Optional<Equipment> equipment = equipmentService.getEquipmentById(id);
        return equipment.orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipment not found with id: " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Equipment addEquipment(@RequestBody Equipment equipment) {
        try {
            if (equipment.getLocation() != null && equipment.getLocation().getAddress() != null
                    && !equipment.getLocation().getAddress().isEmpty()) {
                Location geoLocation = geoapifyService.geocode(equipment.getLocation().getAddress());
                Location savedLocation = locationService.addLocation(geoLocation); // Salviamo la location prima
                equipment.setLocation(savedLocation);
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "L'indirizzo della location è obbligatorio per postare l'attrezzatura.");
            }
            return equipmentService.addEquipment(equipment);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Errore durante la geocodifica dell'indirizzo: " + e.getMessage(), e);
        }
    }

    @PutMapping("/{id}")
    public Equipment updateEquipment(@PathVariable Long id, @RequestBody Equipment updatedEquipment) {
        return equipmentService.updateEquipment(id, updatedEquipment);
    }

    @DeleteMapping("/{id}")
    public void deleteEquipment(@PathVariable Long id) {
        equipmentService.deleteEquipment(id);
    }
}
