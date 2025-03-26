package com.navigram.server.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.navigram.server.dto.SocialUserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

@Service
public class Auth0Service {
    private static final Logger logger = LoggerFactory.getLogger(Auth0Service.class);

    @Value("${auth0.domain}")
    private String domain;

    @Value("${auth0.audience}")
    private String audience;
    
    @Value("${auth0.client-id}")
    private String clientId;
    
    @Value("${auth0.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public Auth0Service() {
        logger.info("Auth0Service initialized with domain: {}", domain);
    }

    /**
     * Verifies the Auth0 token and extracts user information
     * by using Auth0 userinfo endpoint
     *
     * @param token Auth0 ID token
     * @return SocialUserInfo containing user details
     */
    public SocialUserInfo verifyTokenAndGetUser(String token) {
        try {
            logger.debug("Starting token verification using Auth0 userinfo endpoint");
            
            // Instead of decoding the token, use Auth0's userinfo endpoint to get user data
            String userInfoUrl = "https://" + domain + "/userinfo";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            logger.debug("Calling Auth0 userinfo endpoint: {}", userInfoUrl);
            ResponseEntity<Map> response = restTemplate.exchange(
                userInfoUrl,
                HttpMethod.GET,
                entity,
                Map.class
            );
            
            logger.debug("Auth0 userinfo response received: {}", response.getStatusCode());
            
            // Extract user information from the response
            Map<String, Object> userInfo = response.getBody();
            if (userInfo == null) {
                throw new IllegalArgumentException("No user info returned from Auth0");
            }
            
            String userId = (String) userInfo.get("sub");
            if (userId == null) {
                logger.error("User info missing subject claim");
                throw new IllegalArgumentException("Invalid user info: missing subject claim");
            }
            
            // Log the full user info for debugging
            logger.debug("Auth0 user info: {}", userInfo);
            
            String email = (String) userInfo.get("email");
            if (email == null) {
                logger.error("User info missing email claim");
                throw new IllegalArgumentException("Invalid user info: missing email claim");
            }
            
            String name = (String) userInfo.get("name");
            String picture = (String) userInfo.get("picture");
            
            logger.info("Successfully extracted user info - ID: {}, Email: {}", userId, email);
            
            return new SocialUserInfo(
                userId,
                email, 
                name != null ? name : email,
                picture, 
                "auth0"
            );
        } catch (Exception e) {
            logger.error("Error verifying Auth0 token", e);
            throw new RuntimeException("Failed to verify Auth0 token: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validates an Auth0 access token by calling the userinfo endpoint
     *
     * @param token The Auth0 access token
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            logger.debug("Validating token using Auth0 userinfo endpoint");
            
            String userInfoUrl = "https://" + domain + "/userinfo";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                userInfoUrl,
                HttpMethod.GET,
                entity,
                Map.class
            );
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            logger.error("Error validating Auth0 token", e);
            return false;
        }
    }
} 