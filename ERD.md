erDiagram

USER {
    String id PK
    String username
    String email
    String password
    Role role
    boolean accountNonExpired
    boolean accountNonLocked
    boolean credentialsNonExpired
    boolean enabled
}

MEMORY {
    String id PK
    String user_id FK
    String mediaUrl
    MediaType mediaType
    String description
    Point location
    LocalDateTime createdAt
    int upvoteCount
    VisibilityType visibility
    String audioUrl
}

COMMENT {
    String id PK
    String memory_id FK
    String user_id FK
    String content
    LocalDateTime createdAt
}

FLAG {
    String id PK
    String memory_id FK
    String reason
    LocalDateTime createdAt
    boolean resolved
}

USER ||--o{ MEMORY : creates
USER ||--o{ COMMENT : creates
MEMORY ||--o{ COMMENT : has
MEMORY ||--o{ FLAG : has
