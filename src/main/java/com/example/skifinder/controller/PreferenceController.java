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

    @GetMapping
    public List<Preference> getAllPreferences() {
        return preferenceService.getAllPreferences();
    }

    @PostMapping
    public Preference addPreference(@RequestBody Preference preference) {
        return preferenceService.addPreference(preference);
    }

    @GetMapping("/user")
    public List<Preference> getPreferencesByUserId(@RequestParam Long userId) {
        return preferenceService.getPreferencesByUserId(userId);
    }

    @DeleteMapping("/{id}")
    public void removePreference(@PathVariable Long id) {
        preferenceService.removePreference(id);
    }
}