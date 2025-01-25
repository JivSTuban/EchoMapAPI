package com.echomap.server.controller;

import com.echomap.server.dto.AuthRequest;
import com.echomap.server.dto.AuthResponse;
import com.echomap.server.dto.UserDto;
import com.echomap.server.model.User;
import com.echomap.server.security.JwtTokenProvider;
import com.echomap.server.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest loginRequest) {
        logger.info("Received login request for username: {}", loginRequest.getUsername());
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(new AuthResponse(jwt, user.getId(), user.getUsername(), user.getEmail(), user.getRole()));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody UserDto userDto) {
        UserDto createdUser = userService.createUser(userDto);

        // Authenticate the new user
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                userDto.getUsername(),
                userDto.getPassword()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        return ResponseEntity.ok(new AuthResponse(jwt, createdUser.getId(), createdUser.getUsername(), createdUser.getEmail(), ((User) authentication.getPrincipal()).getRole()));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        logger.info("Received request to get current user");
        try {
            if (authentication == null) {
                logger.warn("No authentication found");
                return ResponseEntity.status(401).body("No authentication found");
            }

            String username = authentication.getName();
            UserDto userDto = userService.getUserByUsername(username);

            logger.info("Authenticated user: {}", username);
            String currentToken = tokenProvider.generateToken(authentication);
            logger.info("Generated fresh token for user: {}", username);

            return ResponseEntity.ok(new AuthResponse(
                currentToken,
                userDto.getId(),
                userDto.getUsername(),
                userDto.getEmail(),
                userDto.getRole()
            ));
        } catch (Exception e) {
            logger.error("Error getting current user", e);
            return ResponseEntity.status(401).body("Authentication failed: " + e.getMessage());
        }
    }
}
