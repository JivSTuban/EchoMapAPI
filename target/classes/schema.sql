CREATE DATABASE IF NOT EXISTS echomap;
USE echomap;

CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('USER', 'SUPER_USER', 'MODERATOR', 'ADMIN', 'GUEST') NOT NULL DEFAULT 'USER',
    account_non_expired BOOLEAN DEFAULT TRUE,
    account_non_locked BOOLEAN DEFAULT TRUE,
    credentials_non_expired BOOLEAN DEFAULT TRUE,
    enabled BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS memories (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    audio_url VARCHAR(255),
    location POINT NOT NULL SRID 4326,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    upvote_count INT DEFAULT 0,
    visibility ENUM('PUBLIC', 'PRIVATE', 'FOLLOWERS') DEFAULT 'PUBLIC',
    SPATIAL INDEX(location),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS flags (
    id VARCHAR(36) PRIMARY KEY,
    memory_id VARCHAR(36) NOT NULL,
    reason VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (memory_id) REFERENCES memories(id) ON DELETE CASCADE
);

-- Create spatial index for efficient nearby queries
ALTER TABLE memories ADD SPATIAL INDEX(location);

-- Add index for memory visibility queries
CREATE INDEX idx_memory_visibility ON memories(visibility);

-- Add index for user lookups by username and email
CREATE INDEX idx_user_username ON users(username);
CREATE INDEX idx_user_email ON users(email);