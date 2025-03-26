-- Remove CREATE DATABASE and USE commands for H2 compatibility

CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    account_non_expired BOOLEAN DEFAULT TRUE,
    account_non_locked BOOLEAN DEFAULT TRUE,
    credentials_non_expired BOOLEAN DEFAULT TRUE,
    enabled BOOLEAN DEFAULT TRUE,
    CONSTRAINT uk_username UNIQUE (username),
    CONSTRAINT uk_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS memories (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    media_url VARCHAR(255),
    media_type VARCHAR(10) NOT NULL,
    cloudinary_public_id VARCHAR(255),
    title VARCHAR(255),
    description TEXT,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    upvote_count INT DEFAULT 0,
    visibility VARCHAR(20) DEFAULT 'PUBLIC',
    is_flagged BOOLEAN DEFAULT FALSE,
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
-- H2 doesn't support SPATIAL INDEX in this form
-- ALTER TABLE memories ADD SPATIAL INDEX(location);

-- Add index for memory visibility queries
CREATE INDEX IF NOT EXISTS idx_memory_visibility ON memories(visibility);

-- Add index for user lookups by username and email
CREATE INDEX IF NOT EXISTS idx_user_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);

-- Add index for memory media type queries
CREATE INDEX IF NOT EXISTS idx_memory_media_type ON memories(media_type);

-- Add index for cloudinary public id
CREATE INDEX IF NOT EXISTS idx_memory_cloudinary_id ON memories(cloudinary_public_id);