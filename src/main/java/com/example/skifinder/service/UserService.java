package com.example.skifinder.service;

import com.example.skifinder.model.Equipment;
import com.example.skifinder.model.User;
import com.example.skifinder.repository.UserRepository;
import com.example.skifinder.repository.EquipmentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    public List<Equipment> getRentedEquipment(Long userId) {
        return equipmentRepository.findByBookingsUserId(userId);
    }

    public List<Equipment> getOwnedEquipment(Long userId) {
        // Filtra tutta l’attrezzatura e trova quella in cui Equipment.user.id == userId
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