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
        // 1) cerco l’utente per username
        Optional<User> optUser = userRepository.findByUsername(login);
        if (optUser.isEmpty()) {
            // 2) provo email
            optUser = userRepository.findByEmail(login);
            if (optUser.isEmpty()) {
                // 3) provo phone
                optUser = userRepository.findByPhoneNumber(login);
                if (optUser.isEmpty()) {
                    throw new UsernameNotFoundException("Utente non trovato: " + login);
                }
            }
        }

        User user = optUser.get();

        // Converto 'User' in un 'UserDetails' di Spring Security
        // (che include username, password, e roles/authorities)
        return org.springframework.security.core.userdetails.User.builder()
                .username(preferredLogin(user)) // per "username" ci mettiamo username/email/phone
                .password(user.getPassword()) // password hashed
                .roles(user.getRole().name()) // ruoli
                .build();
    }

    // Se vuoi, decidi quale campo mostrare come "username" preferito
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
