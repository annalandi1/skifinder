package com.example.skifinder.controller;

import com.example.skifinder.model.User;
import com.example.skifinder.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Controller per operazioni sugli utenti (visualizzazione, creazione, ecc.)
 * ATTENZIONE: la registrazione con password encoder è già gestita da
 * AuthController.
 * Se vuoi, puoi rimuovere "POST /api/users" o usarlo solo in contesti di admin.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Recupera tutti gli utenti
     * GET /api/users
     */
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    /**
     * Recupera utente per ID
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + id);
        }
        return user;
    }

    /**
     * Crea un nuovo utente (sconsigliato se non necessario)
     * POST /api/users
     */
    @PostMapping
    public User addUser(@RequestBody User user) {
        return userService.addUser(user);
    }
}
