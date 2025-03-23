package com.example.skifinder.controller;

import com.example.skifinder.model.Preference;
import com.example.skifinder.service.PreferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/preference")
public class PreferenceController {

    @Autowired
    private PreferenceService preferenceService;

    // 🔹 Ottieni tutte le preferenze
    @GetMapping
    public List<Preference> getAllPreferences() {
        return preferenceService.getAllPreferences();
    }

    // 🔹 Aggiungi una preferenza (passando userId ed equipmentId)
    @PostMapping
    public Preference addPreference(@RequestParam Long userId, @RequestParam Long equipmentId) {
        return preferenceService.addPreference(userId, equipmentId);
    }

    // 🔹 Ottieni le preferenze di un utente
    @GetMapping("/user")
    public List<Preference> getPreferencesByUserId(@RequestParam Long userId) {
        return preferenceService.getPreferencesByUserId(userId);
    }

    // 🔹 Rimuovi una preferenza (passando userId ed equipmentId)
    @DeleteMapping
    public void removePreference(@RequestParam Long userId, @RequestParam Long equipmentId) {
        preferenceService.removePreference(userId, equipmentId);
    }
}
