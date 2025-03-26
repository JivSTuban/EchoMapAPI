package com.navigram.server.dto;

import com.navigram.server.model.Role;

import java.time.LocalDateTime;

public class AuthResponse {
    private String token;
    private String id;
    private String username;
    private String email;
    private String name;
    private String profilePicture;
    private Role role;
    private boolean phoneVerified;
    private String phoneNumber;
    private LocalDateTime createdAt;

    public AuthResponse() {
    }

    public AuthResponse(String token, String id, String username, String email, Role role) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    public AuthResponse(String token, String id, String username, String email, String name, Role role) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    public AuthResponse(String token, String id, String username, String email, Role role, String phoneNumber, boolean phoneVerified) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.phoneNumber = phoneNumber;
        this.phoneVerified = phoneVerified;
    }
    
    public AuthResponse(String token, String id, String username, String email, String name, Role role, String phoneNumber, boolean phoneVerified) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.name = name;
        this.role = role;
        this.phoneNumber = phoneNumber;
        this.phoneVerified = phoneVerified;
    }
    
    public AuthResponse(String id, String username, String email, String profilePicture, String token, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.profilePicture = profilePicture;
        this.token = token;
        this.createdAt = createdAt;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getProfilePicture() {
        return profilePicture;
    }
    
    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isPhoneVerified() {
        return phoneVerified;
    }

    public void setPhoneVerified(boolean phoneVerified) {
        this.phoneVerified = phoneVerified;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
