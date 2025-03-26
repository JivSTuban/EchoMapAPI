package com.navigram.server.model;

public enum Role {
    USER,           // Basic user with standard permissions
    SUPER_USER,     // User with additional privileges
    MODERATOR,      // Can moderate content and users
    ADMIN,         // Full system access
    GUEST          // Guest user with limited permissions
}
