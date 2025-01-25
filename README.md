# EchoMap Server

EchoMap is an anonymous, location-based memory sharing platform that allows users to leave voice notes, stories, or art tied to GPS coordinates, creating an invisible layer of collective memory. Users can create and manage their accounts, upload and share audio memories tied to specific locations, browse nearby memories with customizable radius, set privacy settings for their memories, flag inappropriate content, and perform spatial queries for efficient nearby memory retrieval.

## Features

- Create and manage user accounts
- Upload and share audio memories tied to specific locations
- Browse nearby memories with customizable radius
- Privacy settings for memories (public, private, followers)
- Flag inappropriate content
- Spatial queries for efficient nearby memory retrieval
- Follow/unfollow users
- Time-decay mechanism for memories
- Admin moderation tools

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

## API Endpoints

<details>
<summary>Public Endpoints</summary>

#### User Management

- `POST /api/users` - Create new user
- `GET /api/users/username/{username}` - Get user by username

#### Authentication

- `POST /api/auth/login` - Login user
- `POST /api/auth/register` - Register new user
- `POST /api/guest/auth/login` - Guest login
- `POST /api/guest/auth/register` - Auto-generated guest account registration and login

#### Memory Management

- `GET /api/memories/nearby/public` - Get nearby public memories

</details>

<details>
<summary>Protected Endpoints</summary>

#### User Management

- `GET /api/users/{id}` - Get user by ID
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user
- `POST /api/users/{id}/follow` - Follow a user
- `DELETE /api/users/{id}/unfollow` - Unfollow a user

#### Authentication

- `GET /api/auth/me` - Get current user

#### Memory Management

- `POST /api/memories` - Create new memory
- `GET /api/memories/{id}` - Get memory by ID
- `GET /api/memories/nearby` - Get nearby memories (authenticated)
- `PUT /api/memories/{id}` - Update memory
- `DELETE /api/memories/{id}` - Delete memory

#### Comment Management

- `POST /api/comments` - Create new comment
- `GET /api/comments/memory/{memoryId}` - Get comments for a memory
- `DELETE /api/comments/{id}` - Delete comment

#### Flag Management

- `POST /api/flags` - Flag a memory
- `GET /api/flags/memory/{memoryId}` - Get flags for a memory
- `GET /api/flags/memory/{memoryId}/status` - Check if memory is flagged
- `PATCH /api/flags/{id}/resolve` - Resolve a flag
- `DELETE /api/flags/memories/{id}/hide` - Hide a memory

</details>

## Request Examples

### Create User
```json
POST /api/users
Request:
{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "password123"
}

Response:
{
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_doe",
    "email": "john@example.com",
    "role": "USER"
}
```

### Get User
```json
GET /api/users/550e8400-e29b-41d4-a716-446655440000

Response:
{
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_doe",
    "email": "john@example.com",
    "role": "USER"
}
```

### Update User
```json
PUT /api/users/550e8400-e29b-41d4-a716-446655440000
Request:
{
    "username": "john_doe_updated",
    "email": "john_updated@example.com"
}

Response:
{
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_doe_updated",
    "email": "john_updated@example.com",
    "role": "USER"
}
```

### Delete User
```json
DELETE /api/users/550e8400-e29b-41d4-a716-446655440000

Response:
{
    "message": "User deleted successfully."
}
```

### Follow User
```json
POST /api/users/550e8400-e29b-41d4-a716-446655440000/follow

Response:
{
    "message": "User followed successfully."
}
```

### Unfollow User
```json
DELETE /api/users/550e8400-e29b-41d4-a716-446655440000/unfollow

Response:
{
    "message": "User unfollowed successfully."
}
```

### Login User
```json
POST /api/auth/login
Request:
{
    "username": "john_doe",
    "password": "password123"
}

Response:
{
    "token": "jwt-token-here",
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_doe",
    "email": "john@example.com",
    "role": "USER"
}
```

### Register User
```json
POST /api/auth/register
Request:
{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "password123"
}

Response:
{
    "token": "jwt-token-here",
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_doe",
    "email": "john@example.com",
    "role": "USER"
}
```

### Guest Login
```json
POST /api/guest/auth/login
Request:
{
    "username": "guest_user",
    "password": "guest_password"
}

Response:
{
    "token": "jwt-token-here",
    "id": "guest-uuid",
    "username": "guest_user",
    "email": "guest@example.com",
    "role": "GUEST"
}
```

### Guest Register
```json
POST /api/guest/auth/register

Response:
{
    "token": "jwt-token-here",
    "id": "guest-uuid",
    "username": "guest_user",
    "email": "guest@example.com",
    "role": "GUEST"
}
```

### Get Current User
```json
GET /api/auth/me

Response:
{
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_doe",
    "email": "john@example.com",
    "role": "USER"
}
```

### Create Memory
```json
POST /api/memories
Request:
{
    "latitude": 37.7749,
    "longitude": -122.4194,
    "audioUrl": "https://storage.example.com/audio123.mp3",
    "visibility": "PUBLIC"
}

Response:
{
    "id": "memory-uuid",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_doe",
    "audioUrl": "https://storage.example.com/audio123.mp3",
    "createdAt": "2024-01-21T04:24:00",
    "upvoteCount": 0,
    "visibility": "PUBLIC",
    "latitude": 37.7749,
    "longitude": -122.4194
}
```

### Get Memory
```json
GET /api/memories/memory-uuid

Response:
{
    "id": "memory-uuid",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_doe",
    "audioUrl": "https://storage.example.com/audio123.mp3",
    "createdAt": "2024-01-21T04:24:00",
    "upvoteCount": 0,
    "visibility": "PUBLIC",
    "latitude": 37.7749,
    "longitude": -122.4194
}
```

### Update Memory
```json
PUT /api/memories/memory-uuid
Request:
{
    "mediaUrl": "https://storage.example.com/media123_updated.mp3",
    "mediaType": "AUDIO",
    "description": "Updated description",
    "visibility": "PRIVATE",
    "latitude": 37.7749,
    "longitude": -122.4194,
    "audioUrl": "https://storage.example.com/audio123_updated.mp3"
}

Response:
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
    "longitude": -122.4194,
    "audioUrl": "https://storage.example.com/audio123_updated.mp3"
}
```

### Delete Memory
```json
DELETE /api/memories/memory-uuid

Response:
{
    "message": "Memory deleted successfully."
}
```

### Create Comment
```json
POST /api/comments
Request:
{
    "memoryId": "memory-uuid",
    "content": "Great memory!"
}

Response:
{
    "id": "comment-uuid",
    "memoryId": "memory-uuid",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_doe",
    "content": "Great memory!",
    "createdAt": "2024-01-21T04:24:00"
}
```

### Get Comments for Memory
```json
GET /api/comments/memory/memory-uuid

Response:
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

### Delete Comment
```json
DELETE /api/comments/comment-uuid

Response:
{
    "message": "Comment deleted successfully."
}
```

### Flag Memory
```json
POST /api/flags
Request:
{
    "memoryId": "memory-uuid",
    "reason": "Inappropriate content"
}

Response:
{
    "id": "flag-uuid",
    "memoryId": "memory-uuid",
    "memoryUserId": "550e8400-e29b-41d4-a716-446655440000",
    "reason": "Inappropriate content",
    "createdAt": "2024-01-21T04:24:00"
}
```

### Get Flags for Memory
```json
GET /api/flags/memory/memory-uuid

Response:
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

### Check Memory Flag Status
```json
GET /api/flags/memory/memory-uuid/status

Response:
{
    "flagged": true
}
```

### Resolve Flag
```json
PATCH /api/flags/flag-uuid/resolve

Response:
{
    "message": "Flag resolved successfully."
}
```

### Hide Memory
```json
DELETE /api/flags/memories/memory-uuid/hide

Response:
{
    "message": "Memory hidden successfully."
}
```

## Development

### Database Schema

The application uses MySQL with spatial extensions. Key tables:

- `users` - User management
- `memories` - Audio memories with spatial data
- `comments` - Comments on memories
- `flags` - Content moderation

For a detailed visualization of the database schema, refer to the [ERD](https://mermaid.live/view#pako:eNqdU9tOwzAM_ZUqz-MH-jZtgwfWDe2CBKqEvMRs0Zq4StJBNfh33MsGZVQg-lDZ55wkPk58FJIUiligG2vYOjCpTe16OVlEx9RG_C2D03YbaRXd3XaQwqOzYLADogGddZAcvH8hpxpwQRlGjn9NuiEOwUYgJRU2zMhOXnPtUPXRU5L776xkPdqgIfN9G6CFTVaB75W9ZJLMFw9_MvjE8HUXNqg0rF3rMqmyVZljg1dRR63QS6fzoMk2-B1pG6KMJHxi7AqyMQRcaYOVHw7VMDRkJS_yAwUcVU1owHvt9UZnOpT12Ydz2jkcCqWpLrX2PZonyWS2-tW4QUOu_MF6T0ck2YCnynq91CVcT4c3_z-fN_N_6tnp3h16yg7ni6_f9dvb1RUdo_YRxO1S32VPrfpCtwsuBDu4IGuXLSMGwqDjsVA8ZbXzVIQd8tyImEMFbp8Kro91UARallaKOLgCB8JRsd2J-JkfNmdFrriQdkpPkhzsI5FpRe8fnHNJpQ) file.

### Environment Variables

- `SPRING_DATASOURCE_URL` - MySQL connection URL
- `SPRING_DATASOURCE_USERNAME` - Database username
- `SPRING_DATASOURCE_PASSWORD` - Database password

## License

MIT License
