package com.navigram.server.controller;

import com.navigram.server.dto.AuthResponse;
import com.navigram.server.dto.SocialUserInfo;
import com.navigram.server.model.User;
import com.navigram.server.model.Role;
import com.navigram.server.service.Auth0Service;
import com.navigram.server.service.UserService;
import com.navigram.server.security.JwtTokenProvider;
import com.navigram.server.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/social")
@CrossOrigin(origins = {"http://localhost:5173", "https://echo-map-frontend.vercel.app"}, allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST})
public class SocialAuthController {
    private static final Logger logger = LoggerFactory.getLogger(SocialAuthController.class);

    private final Auth0Service auth0Service;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final UserRepository userRepository;

    @Autowired
    public SocialAuthController(Auth0Service auth0Service, JwtTokenProvider jwtTokenProvider, UserService userService, UserRepository userRepository) {
        this.auth0Service = auth0Service;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
        this.userRepository = userRepository;
        logger.info("SocialAuthController initialized");
    }

    @PostMapping("/auth0/exchange")
    public ResponseEntity<?> exchangeAuth0Token(HttpServletRequest request) {
        logger.info("Received Auth0 token exchange request from: {}", request.getRemoteAddr());
        
        try {
            // Extract token from Authorization header
            String authHeader = request.getHeader("Authorization");
            logger.debug("Authorization header: {}", authHeader);
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Invalid or missing Authorization header");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid Authorization header format");
            }
            
            String token = authHeader.substring(7);
            logger.debug("Extracted token: {}", token.substring(0, Math.min(10, token.length())) + "...");
            
            // Verify token and extract user info
            SocialUserInfo userInfo = auth0Service.verifyTokenAndGetUser(token);
            logger.info("Auth0 user verified: {}, sub: {}", userInfo.getEmail(), userInfo.getUserId());
            
            // Use the Auth0 user ID (sub) as the username to ensure consistency
            String auth0UserId = userInfo.getUserId();
            
            // Check if user exists by Auth0 ID, create if not
            User user = userRepository.findByUsername(auth0UserId)
                .orElseGet(() -> {
                    logger.info("Creating new user with Auth0 ID: {}", auth0UserId);
                    // Check if this is an admin user
                    boolean isAdmin = userInfo.getEmail().equals("jeremiatuban@gmail.com");
                    Role userRole = isAdmin ? Role.ADMIN : Role.USER;
                    logger.info("Setting user role to: {} for email: {}", userRole, userInfo.getEmail());
                    
                    return userService.createSocialUser(
                        auth0UserId, // Use Auth0 user ID as username
                        userInfo.getEmail(),
                        userInfo.getName(),
                        userInfo.getProfilePicture(),
                        userInfo.getProvider(),
                        userRole
                    );
                });
                
            // Generate JWT using JwtTokenProvider
            String jwt = jwtTokenProvider.generateToken(user);
            logger.info("JWT token generated successfully for user: {}", user.getEmail());
            
            // Include the username (Auth0 ID) in the response for debugging
            AuthResponse response = new AuthResponse(
                user.getId(),
                user.getUsername(), // This should be the Auth0 ID
                user.getEmail(),
                user.getProfilePicture(),
                jwt,
                user.getCreatedAt()
            );
            response.setRole(user.getRole());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing Auth0 token exchange", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Authentication failed");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse);
        }
    }
    
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> testEndpoint() {
        logger.info("Social auth test endpoint called");
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Social auth endpoint working");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/debug")
    public ResponseEntity<Map<String, Object>> debugEndpoint() {
        logger.info("Debug endpoint called");
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Debug endpoint working");
        response.put("timestamp", System.currentTimeMillis());
        response.put("controllerName", this.getClass().getSimpleName());
        return ResponseEntity.ok(response);
    }
    
    private String generateUniqueUsername(String name, String email) {
        // Try to use the name first, fallback to email if needed
        String baseUsername = "";
        
        if (name != null && !name.trim().isEmpty()) {
            // Convert name to lowercase, replace spaces with dots
            baseUsername = name.toLowerCase().trim().replace(" ", ".");
        } else {
            // Fallback to email prefix
            baseUsername = email.split("@")[0];
        }
        
        // Replace any non-alphanumeric characters
        baseUsername = baseUsername.replaceAll("[^a-zA-Z0-9.]", "");
        
        // Check if username exists, if so add numbers until unique
        String username = baseUsername;
        int counter = 1;
        
        while (userService.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }
        
        return username;
    }
} 