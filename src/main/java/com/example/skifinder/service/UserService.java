package com.example.skifinder.service;

import com.example.skifinder.model.Equipment;
import com.example.skifinder.model.User;
import com.example.skifinder.repository.UserRepository;
import com.example.skifinder.repository.EquipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    // Trova utente dal "login" (può essere username, email o telefono)
    public User getUserByLogin(String login) {
        return userRepository.findByUsername(login)
                .or(() -> userRepository.findByEmail(login))
                .or(() -> userRepository.findByPhoneNumber(login))
                .orElse(null);
    }

    // Aggiorna dati utente loggato (solo campi modificabili)
    public User updateCurrentUser(String login, User updatedData) {
        User user = getUserByLogin(login);
        if (user == null)
            return null;

        // Aggiorno i campi modificabili
        user.setName(updatedData.getName());
        user.setSurname(updatedData.getSurname());
        user.setUsername(updatedData.getUsername());
        user.setEmail(updatedData.getEmail());
        user.setPhoneNumber(updatedData.getPhoneNumber());
        user.setRole(updatedData.getRole());
        user.setPhoto(updatedData.getPhoto());

        return userRepository.save(user);
    }

    @Transactional
    public void updatePhoto(String username, String photoPath) {
        System.out.println("🧠 updatePhoto(): username = " + username + ", path = " + photoPath);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utente non trovato: " + username));

        user.setPhoto(photoPath);
        System.out.println("📸 Salvo l'utente aggiornato: " + user.getPhoto());
        userRepository.saveAndFlush(user); // Forza il commit immediato

        System.out.println("✅ Foto aggiornata nel DB per l'utente: " + username);
    }

    public List<Equipment> getRentedEquipment(Long userId) {
        return equipmentRepository.findByBookingsUserId(userId);
    }

    public List<Equipment> getOwnedEquipment(Long userId) {
        return equipmentRepository.findAll().stream()
                .filter(e -> e.getUser() != null && e.getUser().getId().equals(userId))
                .toList();
    }

    public User addUser(User user) {
        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
