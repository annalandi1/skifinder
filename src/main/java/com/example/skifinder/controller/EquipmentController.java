package com.example.skifinder.controller;

import com.example.skifinder.model.Equipment;
import com.example.skifinder.model.Location;
import com.example.skifinder.service.EquipmentService;
import com.example.skifinder.service.GeoapifyService;
import com.example.skifinder.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    @Autowired
    private EquipmentService equipmentService;

    @Autowired
    private GeoapifyService geoapifyService;

    @Autowired
    private LocationService locationService;

    private final Path uploadDir = Paths.get("uploads"); // Cartella dove salvare le immagini

    public EquipmentController() {
        try {
            Files.createDirectories(uploadDir); // Crea la cartella se non esiste
        } catch (IOException e) {
            throw new RuntimeException("Errore nella creazione della cartella upload", e);
        }
    }

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

    @PostMapping("/{id}/upload-images")
    public ResponseEntity<String> uploadImages(
            @PathVariable Long id,
            @RequestParam("files") MultipartFile[] files) {

        Optional<Equipment> optionalEquipment = equipmentService.getEquipmentById(id);
        if (optionalEquipment.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Attrezzatura non trovata");
        }

        Equipment equipment = optionalEquipment.get();

        // Controlliamo il numero di immagini caricate
        if (files.length < 1 || files.length > 4) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Devi caricare almeno 1 immagine e massimo 4 immagini");
        }

        List<String> imagePaths = new ArrayList<>();
        Path uploadDir = Paths.get("uploads/equipment");

        try {
            for (MultipartFile file : files) {
                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path filePath = uploadDir.resolve(fileName);

                // Salviamo il file sul server
                Files.copy(file.getInputStream(), filePath);

                // Aggiungiamo il percorso alla lista
                imagePaths.add(filePath.toString());
            }

            equipment.setImagePaths(imagePaths);
            equipmentService.updateEquipment(id, equipment);

            return ResponseEntity.ok("Immagini caricate con successo");

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Errore durante il caricamento delle immagini", e);
        }
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> getImage(@PathVariable Long id) {
        Optional<Equipment> optionalEquipment = equipmentService.getEquipmentById(id);
        if (optionalEquipment.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Equipment equipment = optionalEquipment.get();

        // Controlliamo se ci sono immagini disponibili
        if (equipment.getImagePaths() == null || equipment.getImagePaths().isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        // Prendiamo la prima immagine dalla lista
        Path filePath = Paths.get(equipment.getImagePaths().get(0));

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Errore durante il recupero dell'immagine", e);
        }
    }

    @GetMapping("/{id}/images")
    public ResponseEntity<List<String>> getEquipmentImages(@PathVariable Long id) {
        Optional<Equipment> optionalEquipment = equipmentService.getEquipmentById(id);
        if (optionalEquipment.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Equipment equipment = optionalEquipment.get();
        return ResponseEntity.ok(equipment.getImagePaths());
    }

}
