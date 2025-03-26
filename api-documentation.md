# naviGram Server API Documentation

## Overview
naviGram is a location-based memory sharing platform that allows users to leave voice notes, stories, or art tied to GPS coordinates, creating an invisible layer of collective memory.

## Base URL
```
http://localhost:8080
```

## Authentication
All protected endpoints require a Bearer token in the Authorization header:
```
Authorization: Bearer <token>
```

## Public Endpoints

### User Management

#### Create User
**Endpoint Name**: Create New User
**HTTP Method**: POST
**URL**: `/api/users`
**Description**: Creates a new user account
**Request Headers**:
- Content-Type: application/json
**Request Body**:
```json
{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "password123"
}
```
**Response Example**:
```json
{
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_doe",
    "email": "john@example.com",
    "role": "USER"
}
```
**Response Codes**:
- 200 OK - User created successfully
- 400 Bad Request - Invalid input
- 409 Conflict - Username/email already exists

#### Get User by Username
**Endpoint Name**: Get User by Username
**HTTP Method**: GET
**URL**: `/api/users/username/{username}`
**Description**: Retrieves user information by username
**Request Parameters**:
- username (string) - Username to search for
**Response Example**:
```json
{
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_doe",
    "email": "john@example.com",
    "role": "USER"
}
```
**Response Codes**:
- 200 OK - User found
- 404 Not Found - User not found

### Authentication

#### Login
**Endpoint Name**: User Login
**HTTP Method**: POST
**URL**: `/api/auth/login`
**Description**: Authenticates user and returns JWT token
**Request Headers**:
- Content-Type: application/json
**Request Body**:
```json
{
    "username": "john_doe",
    "password": "password123"
}
```
**Response Example**:
```json
{
    "token": "jwt-token-here",
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_doe",
    "email": "john@example.com",
    "role": "USER"
}
```
**Response Codes**:
- 200 OK - Login successful
- 401 Unauthorized - Invalid credentials

#### Register
**Endpoint Name**: User Registration
**HTTP Method**: POST
**URL**: `/api/auth/register`
**Description**: Registers a new user account
**Request Headers**:
- Content-Type: application/json
**Request Body**:
```json
{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "password123"
}
```
**Response Example**:
```json
{
    "token": "jwt-token-here",
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_doe",
    "email": "john@example.com",
    "role": "USER"
}
```
**Response Codes**:
- 200 OK - Registration successful
- 400 Bad Request - Invalid input
- 409 Conflict - Username/email already exists

#### Guest Login
**Endpoint Name**: Guest User Login
**HTTP Method**: POST
**URL**: `/api/guest/auth/login`
**Description**: Authenticates guest user and returns JWT token
**Request Headers**:
- Content-Type: application/json
**Request Body**:
```json
{
    "username": "guest_user",
    "password": "guest_password"
}
```
**Response Example**:
```json
{
    "token": "jwt-token-here",
    "id": "guest-uuid",
    "username": "guest_user",
    "email": "guest@example.com",
    "role": "GUEST"
}
```
**Response Codes**:
- 200 OK - Login successful
- 401 Unauthorized - Invalid credentials

#### Guest Register
**Endpoint Name**: Guest User Registration
**HTTP Method**: POST
**URL**: `/api/guest/auth/register`
**Description**: Creates a new guest account
**Request Headers**:
- Content-Type: application/json
**Response Example**:
```json
{
    "token": "jwt-token-here",
    "id": "guest-uuid",
    "username": "guest_user",
    "email": "guest@example.com",
    "role": "GUEST"
}
```
**Response Codes**:
- 200 OK - Registration successful
- 500 Internal Server Error - Server error

### Memory Management

#### Get Nearby Public Memories
**Endpoint Name**: Get Nearby Public Memories
**HTTP Method**: GET
**URL**: `/api/memories/nearby/public`
**Description**: Retrieves public memories near a specific location
**Request Parameters**:
- lat (number) - Latitude
- lng (number) - Longitude
- radius (number) - Search radius in kilometers
**Response Example**:
```json
[
    {
        "id": "memory-uuid",
        "userId": "550e8400-e29b-41d4-a716-446655440000",
        "username": "john_doe",
        "mediaUrl": "https://storage.example.com/audio123.mp3",
        "createdAt": "2024-01-21T04:24:00",
        "upvoteCount": 0,
        "visibility": "PUBLIC",
        "latitude": 37.7749,
        "longitude": -122.4194
    }
]
```
**Response Codes**:
- 200 OK - Memories retrieved successfully
- 400 Bad Request - Invalid coordinates

## Protected Endpoints

### User Management

#### Get User by ID
**Endpoint Name**: Get User by ID
**HTTP Method**: GET
**URL**: `/api/users/{id}`
**Description**: Retrieves user information by ID
**Request Headers**:
- Authorization: Bearer <token>
**Request Parameters**:
- id (string) - User ID
**Response Example**:
```json
{
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_doe",
    "email": "john@example.com",
    "role": "USER"
}
```
**Response Codes**:
- 200 OK - User found
- 401 Unauthorized - Invalid token
- 404 Not Found - User not found

#### Update User
**Endpoint Name**: Update User
**HTTP Method**: PUT
**URL**: `/api/users/{id}`
**Description**: Updates user information
**Request Headers**:
- Authorization: Bearer <token>
- Content-Type: application/json
**Request Parameters**:
- id (string) - User ID
**Request Body**:
```json
{
    "username": "john_doe_updated",
    "email": "john_updated@example.com"
}
```
**Response Example**:
```json
{
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_doe_updated",
    "email": "john_updated@example.com",
    "role": "USER"
}
```
**Response Codes**:
- 200 OK - User updated successfully
- 401 Unauthorized - Invalid token
- 404 Not Found - User not found

#### Delete User
**Endpoint Name**: Delete User
**HTTP Method**: DELETE
**URL**: `/api/users/{id}`
**Description**: Deletes a user account
**Request Headers**:
- Authorization: Bearer <token>
**Request Parameters**:
- id (string) - User ID
**Response Example**:
```json
{
    "message": "User deleted successfully."
}
```
**Response Codes**:
- 200 OK - User deleted successfully
- 401 Unauthorized - Invalid token
- 404 Not Found - User not found

#### Follow User
**Endpoint Name**: Follow User
**HTTP Method**: POST
**URL**: `/api/users/{id}/follow`
**Description**: Follows a user
**Request Headers**:
- Authorization: Bearer <token>
**Request Parameters**:
- id (string) - User ID to follow
**Response Example**:
```json
{
    "message": "User followed successfully."
}
```
**Response Codes**:
- 200 OK - Follow successful
- 401 Unauthorized - Invalid token
- 404 Not Found - User not found

#### Unfollow User
**Endpoint Name**: Unfollow User
**HTTP Method**: DELETE
**URL**: `/api/users/{id}/unfollow`
**Description**: Unfollows a user
**Request Headers**:
- Authorization: Bearer <token>
**Request Parameters**:
- id (string) - User ID to unfollow
**Response Example**:
```json
{
    "message": "User unfollowed successfully."
}
```
**Response Codes**:
- 200 OK - Unfollow successful
- 401 Unauthorized - Invalid token
- 404 Not Found - User not found

### Authentication

#### Get Current User
**Endpoint Name**: Get Current User
**HTTP Method**: GET
**URL**: `/api/auth/me`
**Description**: Retrieves current authenticated user information
**Request Headers**:
- Authorization: Bearer <token>
**Response Example**:
```json
{
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_doe",
    "email": "john@example.com",
    "role": "USER"
}
```
**Response Codes**:
- 200 OK - User retrieved successfully
- 401 Unauthorized - Invalid token

### Memory Management

#### Create Memory
**Endpoint Name**: Create Memory
**HTTP Method**: POST
**URL**: `/api/memories`
**Description**: Creates a new memory
**Request Headers**:
- Authorization: Bearer <token>
- Content-Type: application/json
**Request Body**:
```json
{
    "latitude": 37.7749,
    "longitude": -122.4194,
    "mediaUrl": "https://storage.example.com/audio123.mp3",
    "mediaType": "AUDIO",
    "description": "Memory description",
    "visibility": "PUBLIC"
}
```
**Response Example**:
```json
{
    "id": "memory-uuid",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_doe",
    "mediaUrl": "https://storage.example.com/audio123.mp3",
    "mediaType": "AUDIO",
    "description": "Memory description",
    "createdAt": "2024-01-21T04:24:00",
    "upvoteCount": 0,
    "visibility": "PUBLIC",
    "latitude": 37.7749,
    "longitude": -122.4194
}
```
**Response Codes**:
- 200 OK - Memory created successfully
- 401 Unauthorized - Invalid token
- 400 Bad Request - Invalid input

#### Get Memory
**Endpoint Name**: Get Memory
**HTTP Method**: GET
**URL**: `/api/memories/{id}`
**Description**: Retrieves a specific memory
**Request Headers**:
- Authorization: Bearer <token>
**Request Parameters**:
- id (string) - Memory ID
**Response Example**:
```json
{
    "id": "memory-uuid",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_doe",
    "mediaUrl": "https://storage.example.com/audio123.mp3",
    "mediaType": "AUDIO",
    "description": "Memory description",
    "createdAt": "2024-01-21T04:24:00",
    "upvoteCount": 0,
    "visibility": "PUBLIC",
    "latitude": 37.7749,
    "longitude": -122.4194
}
```
**Response Codes**:
- 200 OK - Memory retrieved successfully
- 401 Unauthorized - Invalid token
- 404 Not Found - Memory not found

#### Get Nearby Memories
**Endpoint Name**: Get Nearby Memories
**HTTP Method**: GET
**URL**: `/api/memories/nearby`
**Description**: Retrieves memories near a specific location
**Request Headers**:
- Authorization: Bearer <token>
**Request Parameters**:
- lat (number) - Latitude
- lng (number) - Longitude
- radius (number) - Search radius in kilometers
**Response Example**:
```json
[
    {
        "id": "memory-uuid",
        "userId": "550e8400-e29b-41d4-a716-446655440000",
        "username": "john_doe",
        "mediaUrl": "https://storage.example.com/audio123.mp3",
        "mediaType": "AUDIO",
        "description": "Memory description",
        "createdAt": "2024-01-21T04:24:00",
        "upvoteCount": 0,
        "visibility": "PUBLIC",
        "latitude": 37.7749,
        "longitude": -122.4194
    }
]
```
**Response Codes**:
- 200 OK - Memories retrieved successfully
- 401 Unauthorized - Invalid token
- 400 Bad Request - Invalid coordinates

#### Update Memory
**Endpoint Name**: Update Memory
**HTTP Method**: PUT
**URL**: `/api/memories/{id}`
**Description**: Updates a memory
**Request Headers**:
- Authorization: Bearer <token>
- Content-Type: application/json
**Request Parameters**:
- id (string) - Memory ID
**Request Body**:
```json
{
    "mediaUrl": "https://storage.example.com/media123_updated.mp3",
    "mediaType": "AUDIO",
    "description": "Updated description",
    "visibility": "PRIVATE",
    "latitude": 37.7749,
    "longitude": -122.4194
}
```
**Response Example**:
```json
{
    "id": "memory-uuid",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_doe",
    "mediaUrl": "https://storage.example.com/media123_updated.mp3",
    "mediaType": "AUDIO",
    "description": "Updated description",
    "createdAt": "2024-01-21T04:24:00",
    "upvoteCount": 0,
    "visibility": "PRIVATE",
    "latitude": 37.7749,
    "longitude": -122.4194
}
```
**Response Codes**:
- 200 OK - Memory updated successfully
- 401 Unauthorized - Invalid token
- 404 Not Found - Memory not found
- 403 Forbidden - Not authorized to update memory

#### Delete Memory
**Endpoint Name**: Delete Memory
**HTTP Method**: DELETE
**URL**: `/api/memories/{id}`
**Description**: Deletes a memory
**Request Headers**:
- Authorization: Bearer <token>
**Request Parameters**:
- id (string) - Memory ID
**Response Example**:
```json
{
    "message": "Memory deleted successfully."
}
```
**Response Codes**:
- 200 OK - Memory deleted successfully
- 401 Unauthorized - Invalid token
- 404 Not Found - Memory not found
- 403 Forbidden - Not authorized to delete memory

### Comment Management

#### Create Comment
**Endpoint Name**: Create Comment
**HTTP Method**: POST
**URL**: `/api/comments`
**Description**: Creates a new comment on a memory
**Request Headers**:
- Authorization: Bearer <token>
- Content-Type: application/json
**Request Body**:
```json
{
    "memoryId": "memory-uuid",
    "content": "Great memory!"
}
```
**Response Example**:
```json
{
    "id": "comment-uuid",
    "memoryId": "memory-uuid",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_doe",
    "content": "Great memory!",
    "createdAt": "2024-01-21T04:24:00"
}
```
**Response Codes**:
- 200 OK - Comment created successfully
- 401 Unauthorized - Invalid token
- 404 Not Found - Memory not found

#### Get Comments for Memory
**Endpoint Name**: Get Memory Comments
**HTTP Method**: GET
**URL**: `/api/comments/memory/{memoryId}`
**Description**: Retrieves all comments for a specific memory
**Request Headers**:
- Authorization: Bearer <token>
**Request Parameters**:
- memoryId (string) - Memory ID
**Response Example**:
```json
[
    {
        "id": "comment-uuid",
        "memoryId": "memory-uuid",
        "userId": "550e8400-e29b-41d4-a716-446655440000",
        "username": "john_doe",
        "content": "Great memory!",
        "createdAt": "2024-01-21T04:24:00"
    }
]
```
**Response Codes**:
- 200 OK - Comments retrieved successfully
- 401 Unauthorized - Invalid token
- 404 Not Found - Memory not found

#### Delete Comment
**Endpoint Name**: Delete Comment
**HTTP Method**: DELETE
**URL**: `/api/comments/{id}`
**Description**: Deletes a comment
**Request Headers**:
- Authorization: Bearer <token>
**Request Parameters**:
- id (string) - Comment ID
**Response Example**:
```json
{
    "message": "Comment deleted successfully."
}
```
**Response Codes**:
- 200 OK - Comment deleted successfully
- 401 Unauthorized - Invalid token
- 404 Not Found - Comment not found
- 403 Forbidden - Not authorized to delete comment

### Flag Management

#### Flag Memory
**Endpoint Name**: Flag Memory
**HTTP Method**: POST
**URL**: `/api/flags`
**Description**: Flags a memory for inappropriate content
**Request Headers**:
- Authorization: Bearer <token>
- Content-Type: application/json
**Request Body**:
```json
{
    "memoryId": "memory-uuid",
    "reason": "Inappropriate content"
}
```
**Response Example**:
```json
{
    "id": "flag-uuid",
    "memoryId": "memory-uuid",
    "memoryUserId": "550e8400-e29b-41d4-a716-446655440000",
    "reason": "Inappropriate content",
    "createdAt": "2024-01-21T04:24:00"
}
```
**Response Codes**:
- 200 OK - Memory flagged successfully
- 401 Unauthorized - Invalid token
- 404 Not Found - Memory not found

#### Get Flags for Memory
**Endpoint Name**: Get Memory Flags
**HTTP Method**: GET
**URL**: `/api/flags/memory/{memoryId}`
**Description**: Retrieves all flags for a specific memory
**Request Headers**:
- Authorization: Bearer <token>
**Request Parameters**:
- memoryId (string) - Memory ID
**Response Example**:
```json
[
    {
        "id": "flag-uuid",
        "memoryId": "memory-uuid",
        "memoryUserId": "550e8400-e29b-41d4-a716-446655440000",
        "reason": "Inappropriate content",
        "createdAt": "2024-01-21T04:24:00"
    }
]
```
**Response Codes**:
- 200 OK - Flags retrieved successfully
- 401 Unauthorized - Invalid token
- 404 Not Found - Memory not found

#### Check Memory Flag Status
**Endpoint Name**: Check Memory Flag Status
**HTTP Method**: GET
**URL**: `/api/flags/memory/{memoryId}/status`
**Description**: Checks if a memory has been flagged
**Request Headers**:
- Authorization: Bearer <token>
**Request Parameters**:
- memoryId (string) - Memory ID
**Response Example**:
```json
{
    "flagged": true
}
```
**Response Codes**:
- 200 OK - Status retrieved successfully
- 401 Unauthorized - Invalid token
- 404 Not Found - Memory not found

#### Resolve Flag
**Endpoint Name**: Resolve Flag
**HTTP Method**: PATCH
**URL**: `/api/flags/{id}/resolve`
**Description**: Resolves a flag (admin/moderator only)
**Request Headers**:
- Authorization: Bearer <token>
**Request Parameters**:
- id (string) - Flag ID
**Response Example**:
```json
{
    "message": "Flag resolved successfully."
}
```
**Response Codes**:
- 200 OK - Flag resolved successfully
- 401 Unauthorized - Invalid token
- 403 Forbidden - Not authorized to resolve flags
- 404 Not Found - Flag not found

#### Hide Memory
**Endpoint Name**: Hide Memory
**HTTP Method**: DELETE
**URL**: `/api/flags/memories/{id}/hide`
**Description**: Hides a flagged memory (admin/moderator only)
**Request Headers**:
- Authorization: Bearer <token>
**Request Parameters**:
- id (string) - Memory ID
**Response Example**:
```json
{
    "message": "Memory hidden successfully."
}
```
**Response Codes**:
- 200 OK - Memory hidden successfully
- 401 Unauthorized - Invalid token
- 403 Forbidden - Not authorized to hide memories
- 404 Not Found - Memory not found

## Technology Stack
- Java 17
- Spring Boot 3.2
- MySQL 8.0 with Spatial Extensions
- Docker & Docker Compose
- Maven

## Setup & Installation
1. Clone the repository
2. Make sure you have Docker and Docker Compose installed
3. Run the application:
   ```bash
   docker-compose up --build
   ```

The application will be available at `http://localhost:8080`

## License
MIT License
