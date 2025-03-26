package com.navigram.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * This class was previously used for Firebase authentication.
 * It has been kept as a stub for backward compatibility but is no longer active.
 * The application now uses Auth0 for social authentication.
 */
@Service
public class FirebaseAuthService {
    private static final Logger logger = LoggerFactory.getLogger(FirebaseAuthService.class);

    public FirebaseAuthService() {
        logger.info("FirebaseAuthService is deprecated and will be removed. Using Auth0 for social authentication.");
    }

    public boolean verifyPhoneToken(String idToken) {
        logger.warn("FirebaseAuthService.verifyPhoneToken is deprecated - please use Auth0 instead");
        return false;
    }

    public String getPhoneNumber(String idToken) {
        logger.warn("FirebaseAuthService.getPhoneNumber is deprecated - please use Auth0 instead");
        return null;
    }

    public void revokeToken(String uid) {
        logger.warn("FirebaseAuthService.revokeToken is deprecated - please use Auth0 instead");
    }

    public String createCustomToken(String uid) {
        logger.warn("FirebaseAuthService.createCustomToken is deprecated - please use Auth0 instead");
        return null;
    }
}