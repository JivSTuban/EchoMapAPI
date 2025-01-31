package com.echomap.server.controller;

import com.echomap.server.dto.AuthRequest;
import com.echomap.server.dto.AuthResponse;
import com.echomap.server.dto.Auth0TokenExchangeRequest;
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

    @PostMapping("/auth0/exchange")
    public ResponseEntity<AuthResponse> exchangeAuth0Token(@Valid @RequestBody Auth0TokenExchangeRequest exchangeRequest) {
        logger.info("Received Auth0 token exchange request for email: {}", exchangeRequest.getEmail());
        logger.debug("Auth0 token exchange request details: {}", exchangeRequest);

        try {
            logger.debug("Creating or updating user from Auth0 information");
            // Create or update user from Auth0 information
            UserDto userDto = new UserDto();
            userDto.setEmail(exchangeRequest.getEmail());
            userDto.setUsername(exchangeRequest.getNickname() != null ?
                              exchangeRequest.getNickname() :
                              exchangeRequest.getEmail());
            userDto.setPassword(tokenProvider.generateRandomPassword());

            // Try to find existing user or create new one
            UserDto user = userService.findOrCreateSocialUser(userDto);
            logger.debug("User created or retrieved: {}", user);

            // Create authentication token
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getUsername(),
                null, // No password needed for social login
                userService.getUserAuthorities(user.getRole())
            );
            logger.debug("Authentication token created for user: {}", user.getUsername());

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);
            logger.debug("JWT token generated for user: {}", user.getUsername());

            return ResponseEntity.ok(new AuthResponse(jwt, user.getId(), user.getUsername(), user.getEmail(), user.getRole()));
        } catch (Exception e) {
            logger.error("Error during Auth0 token exchange", e);
            return ResponseEntity.badRequest().build();
        }
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
