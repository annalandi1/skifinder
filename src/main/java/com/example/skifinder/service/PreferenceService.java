package com.example.skifinder.service;

import com.example.skifinder.model.Preference;
import com.example.skifinder.repository.PreferenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PreferenceService {
    @Autowired
    private PreferenceRepository preferenceRepository;

    public List<Preference> getAllPreferences() {
        return preferenceRepository.findAll();
    }

    public Preference addPreference(Preference preference) {
        return preferenceRepository.save(preference);
    }

    public List<Preference> getPreferencesByUserId(Long userId) {
        return preferenceRepository.findByUserId(userId);
    }

    public void removePreference(Long id) {
        preferenceRepository.deleteById(id);
    }

}
