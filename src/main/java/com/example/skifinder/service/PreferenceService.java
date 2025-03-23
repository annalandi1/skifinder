package com.example.skifinder.service;

import com.example.skifinder.model.Preference;
import com.example.skifinder.model.User;
import com.example.skifinder.model.Equipment;
import com.example.skifinder.repository.PreferenceRepository;
import com.example.skifinder.repository.UserRepository;
import com.example.skifinder.repository.EquipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class PreferenceService {
    @Autowired
    private PreferenceRepository preferenceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    // 🔹 Recupera tutte le preferenze
    public List<Preference> getAllPreferences() {
        return preferenceRepository.findAll();
    }

    // 🔹 Aggiunge una preferenza (solo per utenti con ruolo CLIENTE)
    public Preference addPreference(Long userId, Long equipmentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato"));
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attrezzatura non trovata"));

        // Verifica che l'utente sia CLIENTE
        if ("CLIENTE".equals(user.getRole().name())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo gli utenti CLIENTE possono aggiungere preferiti");
        }

        // Evita duplicati
        if (preferenceRepository.findByUserId(userId).stream()
                .anyMatch(p -> p.getEquipment().getId().equals(equipmentId))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "L'attrezzatura è già nei preferiti");
        }

        Preference preference = new Preference();
        preference.setUser(user);
        preference.setEquipment(equipment);
        return preferenceRepository.save(preference);
    }

    // 🔹 Recupera le preferenze di un utente specifico
    public List<Preference> getPreferencesByUserId(Long userId) {
        return preferenceRepository.findByUserId(userId);
    }

    // 🔹 Rimuove una preferenza (solo se appartiene all'utente)
    public void removePreference(Long userId, Long equipmentId) {
        List<Preference> userPreferences = preferenceRepository.findByUserId(userId);
        Optional<Preference> preferenceToRemove = userPreferences.stream()
                .filter(p -> p.getEquipment().getId().equals(equipmentId))
                .findFirst();

        if (preferenceToRemove.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Preferenza non trovata");
        }

        preferenceRepository.delete(preferenceToRemove.get());
    }
}
