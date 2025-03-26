package com.navigram.server.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class Auth0TokenExchangeRequest {
    @NotBlank(message = "Auth0 token is required")
    private String auth0Token;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Name is required")
    private String name;

    // Additional Auth0-specific fields
    private String sub;  // Auth0 user ID
    private String nickname;
    private String picture;

    public String getAuth0Token() {
        return auth0Token;
    }

    public void setAuth0Token(String auth0Token) {
        this.auth0Token = auth0Token;
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

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }
}