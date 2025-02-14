package com.echomap.server.controller;

import com.echomap.server.service.FirebaseAuthService;
import com.echomap.server.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/phone-auth")
public class PhoneAuthController {
    private static final Logger logger = LoggerFactory.getLogger(PhoneAuthController.class);

    private final FirebaseAuthService firebaseAuthService;

    @Autowired
    public PhoneAuthController(FirebaseAuthService firebaseAuthService) {
        this.firebaseAuthService = firebaseAuthService;
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPhoneNumber(@RequestHeader("Authorization") String idToken) {
        try {
            // Remove "Bearer " prefix if present
            idToken = idToken.startsWith("Bearer ") ? idToken.substring(7) : idToken;
            
            if (firebaseAuthService.verifyPhoneToken(idToken)) {
                String phoneNumber = firebaseAuthService.getPhoneNumber(idToken);
                logger.info("Phone number verified via Firebase: {}", phoneNumber);
                return ResponseEntity.ok().body("Phone number verified successfully");
            }
            
            return ResponseEntity.badRequest().body("Invalid phone verification token");
        } catch (Exception e) {
            logger.error("Error during phone verification", e);
            return ResponseEntity.internalServerError().body("Failed to verify phone number: " + e.getMessage());
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getVerificationStatus(@RequestHeader("Authorization") String idToken) {
        try {
            idToken = idToken.startsWith("Bearer ") ? idToken.substring(7) : idToken;
            boolean isVerified = firebaseAuthService.verifyPhoneToken(idToken);
            return ResponseEntity.ok().body(Map.of("verified", isVerified));
        } catch (Exception e) {
            logger.error("Error checking verification status", e);
            return ResponseEntity.internalServerError().body("Failed to check verification status");
        }
    }
}