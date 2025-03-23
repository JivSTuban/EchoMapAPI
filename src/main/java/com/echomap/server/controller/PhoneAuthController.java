package com.echomap.server.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * This controller was previously used for phone authentication via Firebase.
 * It has been kept as a stub for backward compatibility but is no longer active.
 * The application now uses Auth0 for social authentication.
 */
@RestController
@RequestMapping("/api/phone-auth")
public class PhoneAuthController {
    private static final Logger logger = LoggerFactory.getLogger(PhoneAuthController.class);

    public PhoneAuthController() {
        logger.info("PhoneAuthController is deprecated and will be removed. Using Auth0 for social authentication.");
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPhoneNumber(@RequestHeader("Authorization") String idToken) {
        logger.warn("Phone verification via Firebase is deprecated - please use Auth0 instead");
        return ResponseEntity.badRequest().body(Map.of(
            "error", "Phone verification via Firebase is deprecated",
            "message", "Please use Auth0 for authentication"
        ));
    }

    @GetMapping("/status")
    public ResponseEntity<?> getVerificationStatus(@RequestHeader("Authorization") String idToken) {
        logger.warn("Phone verification status check via Firebase is deprecated - please use Auth0 instead");
        return ResponseEntity.badRequest().body(Map.of(
            "error", "Phone verification via Firebase is deprecated",
            "message", "Please use Auth0 for authentication"
        ));
    }
}