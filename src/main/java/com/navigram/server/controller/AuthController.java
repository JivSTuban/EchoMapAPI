package com.navigram.server.controller;

import com.navigram.server.dto.AuthRequest;
import com.navigram.server.dto.AuthResponse;
import com.navigram.server.dto.UserDto;
import com.navigram.server.model.Role;
import com.navigram.server.model.User;
import com.navigram.server.security.JwtTokenProvider;
import com.navigram.server.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtTokenProvider tokenProvider,
            UserService userService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        
        try {
            User user = (User) authentication.getPrincipal();
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("name", user.getName());
            response.put("phoneNumber", user.getPhoneNumber());
            response.put("role", user.getRole());
            response.put("socialLogin", user.isSocialLogin());
            response.put("profilePicture", user.getProfilePicture());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting current user", e);
            return ResponseEntity.internalServerError().body("Error getting user details");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody AuthRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(new AuthResponse(jwt, user.getId(), user.getUsername(), user.getEmail(), user.getName(), user.getRole()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDto userDto) {
        logger.info("Processing registration for user: {}", userDto.getUsername());
        try {
            // Create the user in our system
            UserDto createdUser = userService.createUser(userDto);
            logger.info("User created successfully: {}", createdUser.getUsername());

            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    userDto.getUsername(),
                    userDto.getPassword()
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);

            return ResponseEntity.ok(new AuthResponse(
                jwt,
                createdUser.getId(),
                createdUser.getUsername(),
                createdUser.getEmail(),
                createdUser.getName(),
                Role.USER
            ));

        } catch (Exception e) {
            logger.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
