package com.navigram.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * This class was previously used for Firebase configuration.
 * It has been kept as a placeholder for reference but is no longer active.
 * The application now uses Auth0 for social authentication.
 */
@Configuration
public class FirebaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);
    
    public FirebaseConfig() {
        logger.info("Firebase configuration is no longer active. Using Auth0 for social authentication.");
    }
}