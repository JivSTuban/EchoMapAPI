package com.navigram.server.dto;

public class SocialUserInfo {
    private String userId;
    private String email;
    private String name;
    private String picture;
    private String provider;

    public SocialUserInfo(String userId, String email, String name, String picture, String provider) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.picture = picture;
        this.provider = provider;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicture() {
        return picture;
    }
    
    // Alias for better readability in code
    public String getProfilePicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
    
    @Override
    public String toString() {
        return "SocialUserInfo{" +
                "userId='" + userId + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", provider='" + provider + '\'' +
                '}';
    }
} 