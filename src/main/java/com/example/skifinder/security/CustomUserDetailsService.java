package com.example.skifinder.security;

import com.example.skifinder.model.User;
import com.example.skifinder.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        System.out.println("loadUserByUsername: login ricevuto = " + login);

        // Cerca l'utente per username
        Optional<User> optUser = userRepository.findByUsername(login);
        if (optUser.isEmpty()) {
            // Se non trovato, cerca per email
            optUser = userRepository.findByEmail(login);
            if (optUser.isEmpty()) {
                // Se non trovato, cerca per phoneNumber
                optUser = userRepository.findByPhoneNumber(login);
                if (optUser.isEmpty()) {
                    throw new UsernameNotFoundException("Utente non trovato: " + login);
                }
            }
        }

        User user = optUser.get();
        System.out.println("Utente trovato: " + preferredLogin(user));

        return org.springframework.security.core.userdetails.User.builder()
                .username(preferredLogin(user))
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }

    /**
     * Restituisce il primo valore disponibile tra username, email o phoneNumber
     */
    private String preferredLogin(User user) {
        if (user.getUsername() != null && !user.getUsername().isEmpty()) {
            return user.getUsername();
        } else if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            return user.getEmail();
        } else if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
            return user.getPhoneNumber();
        } else {
            return "User#" + user.getId();
        }
    }
}
