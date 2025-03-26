package com.navigram.server.config;

import com.navigram.server.model.Role;
import com.navigram.server.model.User;
import com.navigram.server.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CreateInitialAdmin implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(CreateInitialAdmin.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CreateInitialAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Check if admin user exists
        if (!userRepository.findByUsername("admin").isPresent()) {
            logger.info("Creating initial admin user...");
            
            User adminUser = new User();
            adminUser.setId(UUID.randomUUID().toString());
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@navigram.com");
            adminUser.setPassword(passwordEncoder.encode("admin12345"));
            adminUser.setRole(Role.ADMIN);
            adminUser.setName("Admin");
            adminUser.setEnabled(true);
            
            userRepository.save(adminUser);
            logger.info("Initial admin user created successfully");
        } else {
            logger.info("Admin user already exists");
        }
    }
} 