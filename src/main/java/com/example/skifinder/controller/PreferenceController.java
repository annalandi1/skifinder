package com.example.skifinder.controller;

import com.example.skifinder.model.Preference;
import com.example.skifinder.service.PreferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/preference")
public class PreferenceController {

    @Autowired
    private PreferenceService preferenceService;

    // Ottieni tutte le preferenze
    @GetMapping
    public List<Preference> getAllPreferences() {
        return preferenceService.getAllPreferences();
    }

    // Aggiungi una preferenza
    @PostMapping
    public Preference addPreference(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long equipmentId) {
        return preferenceService.addPreference(userDetails.getUsername(), equipmentId);
    }

    // Ottieni le preferenze dell'utente autenticato
    @GetMapping("/user")
    public List<Preference> getPreferencesByUser(@AuthenticationPrincipal UserDetails userDetails) {
        return preferenceService.getPreferencesByUsername(userDetails.getUsername());
    }

    // Rimuovi una preferenza (passando userId ed equipmentId)
    @DeleteMapping
    public void removePreference(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long equipmentId) {
        preferenceService.removePreference(userDetails, equipmentId);
    }

}
