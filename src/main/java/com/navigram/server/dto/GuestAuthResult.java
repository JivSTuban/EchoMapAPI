package com.navigram.server.dto;

import com.navigram.server.model.User;

public class GuestAuthResult {
    private final String token;
    private final User user;
    private final String password;

    public GuestAuthResult(String token, User user, String password) {
        this.token = token;
        this.user = user;
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}