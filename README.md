# EchoMap Server

EchoMap is an anonymous, location-based memory sharing platform that allows users to leave voice notes, stories, or art tied to GPS coordinates, creating an invisible layer of collective memory. Users can create and manage their accounts, upload and share audio memories tied to specific locations, browse nearby memories with customizable radius, set privacy settings for their memories, flag inappropriate content, and perform spatial queries for efficient nearby memory retrieval.

## Features

- Create and manage user accounts
- Upload and share audio memories tied to specific locations
- Browse nearby memories with customizable radius
- Privacy settings for memories (public, private, followers)
- Flag inappropriate content
- Spatial queries for efficient nearby memory retrieval

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

### User Management

- `POST /api/users` - Create new user
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/username/{username}` - Get user by username
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### Memory Management

- `POST /api/memories` - Create new memory
- `GET /api/memories/{id}` - Get memory by ID
- `GET /api/memories/nearby` - Get nearby memories (authenticated)
- `GET /api/memories/nearby/public` - Get nearby public memories
- `PUT /api/memories/{id}` - Update memory
- `DELETE /api/memories/{id}` - Delete memory

### Flag Management

- `POST /api/flags` - Flag a memory
- `GET /api/flags/memory/{memoryId}` - Get flags for a memory
- `GET /api/flags/memory/{memoryId}/status` - Check if memory is flagged

## Request Examples

### Create User
```json
POST /api/users
Request:
{
    "username": "john_doe",
    "email": "john@example.com"
}

Response:
{
    "user": {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "username": "john_doe",
        "email": "john@example.com"
    },
    "message": "User created successfully."
}
```

### Get User
```json
GET /api/users/550e8400-e29b-41d4-a716-446655440000

Response:
{
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_doe",
    "email": "john@example.com"
}
```

### Create Memory
```json
POST /api/memories
{
    "latitude": 37.7749,
    "longitude": -122.4194,
    "audioUrl": "https://storage.example.com/audio123.mp3",
    "visibility": "PUBLIC"
}
```

### Flag Memory
```json
POST /api/flags
{
    "memoryId": "memory-uuid",
    "reason": "Inappropriate content"
}
```

## Development

### Database Schema

The application uses MySQL with spatial extensions. Key tables:

- `users` - User management
- `memories` - Audio memories with spatial data
- `flags` - Content moderation

### Environment Variables

- `SPRING_DATASOURCE_URL` - MySQL connection URL
- `SPRING_DATASOURCE_USERNAME` - Database username
- `SPRING_DATASOURCE_PASSWORD` - Database password

## License

MIT License
