package com.example.skifinder.controller;

import com.cloudinary.Cloudinary;
import com.example.skifinder.model.Equipment;
import com.example.skifinder.model.Location;
import com.example.skifinder.model.User;
import com.example.skifinder.service.EquipmentService;
import com.example.skifinder.service.GeoapifyService;
import com.example.skifinder.service.LocationService;
import com.example.skifinder.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/equipment")
@CrossOrigin(origins = "http://localhost:5173")
public class EquipmentController {

    @Autowired
    private EquipmentService equipmentService;

    @Autowired
    private GeoapifyService geoapifyService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private UserService userService;

    @Autowired
    public EquipmentController(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    private final Path uploadDir = Paths.get("uploads");

    public EquipmentController() {
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Errore nella creazione della cartella upload", e);
        }
    }

    @GetMapping
    public List<Equipment> getAllEquipment() {
        return equipmentService.getAllEquipment();
    }

    @GetMapping("/owned")
    public List<Equipment> getOwnedEquipment(@AuthenticationPrincipal UserDetails userDetails) {
        System.out.println("Chiamata a getOwnedEquipment da: " + userDetails.getUsername());
        User user = userService.getUserByLogin(userDetails.getUsername());
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato");
        }
        Long userId = user.getId();
        List<Equipment> ownedEquipment = equipmentService.getEquipmentByUserId(userId);
        System.out.println("Trovate " + ownedEquipment.size() + " attrezzature per l'utente con ID " + userId);
        return ownedEquipment;
    }

    @GetMapping("/{id}")
    public Equipment getEquipmentById(@PathVariable Long id) {
        Optional<Equipment> equipment = equipmentService.getEquipmentById(id);
        return equipment.orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipment not found with id: " + id));
    }

    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public Equipment addEquipmentMultipart(
            @RequestPart("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") Double price,
            @RequestParam("size") String size,
            @RequestParam("type") String type,
            @RequestParam("isAvailable") Boolean isAvailable,
            @RequestParam("location") String locationStr,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("Richiesta di creazione attrezzatura da: " + userDetails.getUsername());
            User user = userService.getUserByLogin(userDetails.getUsername());
            if (user == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utente non trovato");
            }
            System.out.println("📌 Utente per associazione: " + user.getUsername() + " con ID: " + user.getId());

            Map<String, Object> uploadParams = new HashMap<>();
            uploadParams.put("resource_type", "auto");
            Map<?, ?> rawResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            Map<String, Object> uploadResult = new HashMap<>();
            for (Map.Entry<?, ?> entry : rawResult.entrySet()) {
                uploadResult.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            String imageUrl = (String) uploadResult.get("secure_url");
            System.out.println("Immagine caricata: " + imageUrl);

            Location geoLocation = geoapifyService.geocode(locationStr);
            Location savedLocation = locationService.addLocation(geoLocation);
            System.out.println("Località geocodificata: " + savedLocation.getAddress());

            Equipment equipment = new Equipment();
            equipment.setName(name);
            equipment.setDescription(description);
            equipment.setPrice(price);
            equipment.setSize(size);
            equipment.setType(type);
            equipment.setAvailable(isAvailable);
            equipment.setUser(user);
            equipment.setLocation(savedLocation);

            List<String> images = new ArrayList<>();
            images.add(imageUrl);
            equipment.setImagePaths(images);

            Equipment savedEquipment = equipmentService.addEquipment(equipment);
            System.out.println("Attrezzatura creata con ID: " + savedEquipment.getId());
            return savedEquipment;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Errore durante il caricamento dell'attrezzatura: " + e.getMessage(), e);
        }
    }

    @PutMapping("/{id}")
    public Equipment updateEquipment(@PathVariable Long id, @RequestBody Equipment updatedEquipment) {
        System.out.println("Richiesta di aggiornamento per attrezzatura con ID: " + id);
        return equipmentService.updateEquipment(id, updatedEquipment);
    }

    @DeleteMapping("/{id}")
    public void deleteEquipment(@PathVariable Long id) {
        System.out.println("Richiesta di cancellazione per attrezzatura con ID: " + id);
        equipmentService.deleteEquipment(id);
    }

    @PostMapping("/{id}/upload-images")
    public ResponseEntity<String> uploadImages(@PathVariable Long id, @RequestParam("files") MultipartFile[] files) {
        Optional<Equipment> optionalEquipment = equipmentService.getEquipmentById(id);
        if (optionalEquipment.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Attrezzatura non trovata");
        }
        Equipment equipment = optionalEquipment.get();
        if (files.length < 1 || files.length > 4) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Devi caricare almeno 1 immagine e massimo 4 immagini");
        }
        List<String> imageUrls = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                System.out.println("Caricamento immagine in corso...");
                Map<String, Object> uploadParams = new HashMap<>();
                uploadParams.put("resource_type", "auto");

                Map<?, ?> rawResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
                Map<String, Object> uploadResult = new HashMap<>();
                for (Map.Entry<?, ?> entry : rawResult.entrySet()) {
                    uploadResult.put(String.valueOf(entry.getKey()), entry.getValue());
                }
                String imageUrl = (String) uploadResult.get("secure_url");
                imageUrls.add(imageUrl);
                System.out.println("Immagine caricata: " + imageUrl);
            }
            equipment.setImagePaths(imageUrls);
            equipmentService.updateEquipment(id, equipment);
            System.out.println("Attrezzatura aggiornata con immagini.");
            return ResponseEntity.ok("Immagini caricate con successo");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante il caricamento delle immagini");
        }
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> getImage(@PathVariable Long id) {
        Optional<Equipment> optionalEquipment = equipmentService.getEquipmentById(id);
        if (optionalEquipment.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        Equipment equipment = optionalEquipment.get();
        if (equipment.getImagePaths() == null || equipment.getImagePaths().isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
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
