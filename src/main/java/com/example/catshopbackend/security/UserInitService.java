package com.example.catshopbackend.security;

import com.example.catshopbackend.models.User;
import com.example.catshopbackend.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

// Service zum Anlegen der Test-Benutzer
@Service
@RequiredArgsConstructor
public class UserInitService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void initializeUsers() {
        if (userRepository.count() == 0) {
            // SCO-Benutzer erstellen
            createUserIfNotExists("SCO1", "kasse123", List.of("CASHIER"));
            createUserIfNotExists("SCO2", "kasse123", List.of("CASHIER"));
            createUserIfNotExists("SCO3", "kasse123", List.of("CASHIER"));

            // Admin-Benutzer erstellen
            createUserIfNotExists("admin", "admin", List.of("ADMIN", "USER"));
        }
    }

    private void createUserIfNotExists(String username, String password, List<String> roles) {
        if (userRepository.findByUsername(username).isEmpty()) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setRoles(roles);
            userRepository.save(user);
            System.out.println("Benutzer erstellt: " + username + " mit Rollen: " + roles);
        }
    }
}