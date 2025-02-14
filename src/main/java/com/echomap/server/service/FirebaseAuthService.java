package com.echomap.server.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class FirebaseAuthService {
    private static final Logger logger = LoggerFactory.getLogger(FirebaseAuthService.class);
    private final FirebaseAuth firebaseAuth;

    public FirebaseAuthService() {
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    public boolean verifyPhoneToken(String idToken) {
        try {
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);
            String phone = (String) decodedToken.getClaims().get("phone_number");
            logger.info("Successfully verified phone token for number: {}", phone);
            return phone != null;
        } catch (FirebaseAuthException e) {
            logger.error("Error verifying phone token", e);
            return false;
        }
    }

    public void revokeUserTokens(String uid) {
        try {
            firebaseAuth.revokeRefreshTokens(uid);
            logger.info("Successfully revoked tokens for user: {}", uid);
        } catch (FirebaseAuthException e) {
            logger.error("Error revoking tokens for user: {}", uid, e);
            throw new RuntimeException("Failed to revoke user tokens", e);
        }
    }

    public String getPhoneNumber(String idToken) {
        try {
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);
            Map<String, Object> claims = decodedToken.getClaims();
            return (String) claims.get("phone_number");
        } catch (FirebaseAuthException e) {
            logger.error("Error getting phone number from token", e);
            throw new RuntimeException("Failed to get phone number", e);
        }
    }

    public String createCustomToken(String uid) {
        try {
            return firebaseAuth.createCustomToken(uid);
        } catch (FirebaseAuthException e) {
            logger.error("Error creating custom token for user: {}", uid, e);
            throw new RuntimeException("Failed to create custom token", e);
        }
    }
}