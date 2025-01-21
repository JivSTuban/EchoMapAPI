package com.echomap.server.model;

public enum Role {
    USER,           // Basic user with standard permissions
    SUPER_USER,     // User with additional privileges
    MODERATOR,      // Can moderate content and users
    ADMIN          // Full system access
}