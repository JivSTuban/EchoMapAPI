package com.navigram.server.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.navigram.server.dto.AuthRequest;
import com.navigram.server.dto.AuthResponse;
import com.navigram.server.dto.GuestAuthResult;
import com.navigram.server.model.User;
import com.navigram.server.repository.UserRepository;
import com.navigram.server.security.JwtTokenProvider;
import com.navigram.server.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/guest/auth")
@CrossOrigin(origins = "*")
public class GuestAuthController {
    private static final Logger log = LoggerFactory.getLogger(GuestAuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    public GuestAuthController(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest loginRequest) {
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
    public ResponseEntity<AuthResponse> createAndAuthenticateGuest() {
        log.info("Starting guest user creation and authentication");

        GuestAuthResult result = userService.createGuestUser();
        User user = result.getUser();
        String rawPassword = result.getPassword();

        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    user.getUsername(),
                    rawPassword
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);

            log.info("Guest user authenticated successfully: {}", user.getUsername());
            return ResponseEntity.ok(new AuthResponse(jwt, user.getId(), user.getUsername(), user.getEmail(), user.getRole()));
            
        } catch (Exception e) {
            log.error("Failed to authenticate guest user: {}", e.getMessage());
            throw e;
        }
    }
}
