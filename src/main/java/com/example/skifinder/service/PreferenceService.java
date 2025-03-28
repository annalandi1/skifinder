package com.example.skifinder.service;

import com.example.skifinder.model.Preference;
import com.example.skifinder.model.User;
import com.example.skifinder.model.Equipment;
import com.example.skifinder.repository.PreferenceRepository;
import com.example.skifinder.repository.UserRepository;
import com.example.skifinder.repository.EquipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    public Preference addPreference(String username, Long equipmentId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato"));

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attrezzatura non trovata"));

        if (preferenceRepository.findByUserId(user.getId()).stream()
                .anyMatch(p -> p.getEquipment().getId().equals(equipmentId))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "L'attrezzatura è già nei preferiti");
        }

        Preference preference = new Preference(user, equipment);
        return preferenceRepository.save(preference);
    }

    public List<Preference> getPreferencesByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato"));

        return preferenceRepository.findByUserId(user.getId());
    }

    // Rimuove una preferenza (solo se appartiene all'utente)
    public void removePreference(UserDetails userDetails, Long equipmentId) {
        // Trova l'utente a partire dallo username contenuto nel token JWT
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato"));

        // Trova la preferenza dell'utente per quella specifica attrezzatura
        Optional<Preference> preferenceToRemove = preferenceRepository
                .findByUserId(user.getId())
                .stream()
                .filter(p -> p.getEquipment().getId().equals(equipmentId))
                .findFirst();

        if (preferenceToRemove.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Preferenza non trovata");
        }

        preferenceRepository.delete(preferenceToRemove.get());
    }

}
