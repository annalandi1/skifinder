package com.example.skifinder.repository;

import com.example.skifinder.model.Preference;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PreferenceRepository extends JpaRepository<Preference, Long> {

    List<Preference> findByUserId(Long userId);

}
