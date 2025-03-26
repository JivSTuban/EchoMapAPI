package com.navigram.server.service;

import com.navigram.server.dto.CreateMemoryRequest;
import com.navigram.server.dto.MemoryDto;
import com.navigram.server.model.Memory;
import com.navigram.server.model.User;
import com.navigram.server.model.MediaType;
import com.navigram.server.model.VisibilityType;
import com.navigram.server.repository.MemoryRepository;
import com.navigram.server.repository.UserRepository;
import com.navigram.server.util.DtoConverter;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MemoryServiceTest {

    @Mock
    private MemoryRepository memoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DtoConverter dtoConverter;

    @InjectMocks
    private MemoryServiceImpl memoryService;

    private GeometryFactory geometryFactory;
    private User testUser;
    private Memory testMemory;
    private MemoryDto testMemoryDto;
    private CreateMemoryRequest testCreateRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

        // Setup test user
        testUser = new User();
        testUser.setId("userId");
        testUser.setUsername("testuser");

        // Setup test memory
        testMemory = new Memory();
        testMemory.setId("memoryId");
        testMemory.setUser(testUser);
        testMemory.setMediaUrl("https://cloudinary.com/test.jpg");
        testMemory.setCloudinaryPublicId("test/image123");
        testMemory.setMediaType(MediaType.IMAGE);
        testMemory.setDescription("Test description");
        testMemory.setLocation(geometryFactory.createPoint(new Coordinate(1.0, 1.0)));
        testMemory.setVisibility(VisibilityType.PUBLIC);

        // Setup test DTO
        testMemoryDto = new MemoryDto();
        testMemoryDto.setId("memoryId");
        testMemoryDto.setUserId("userId");
        testMemoryDto.setMediaUrl("https://cloudinary.com/test.jpg");
        testMemoryDto.setDescription("Test description");
        testMemoryDto.setLatitude(1.0);
        testMemoryDto.setLongitude(1.0);
        testMemoryDto.setVisibility(VisibilityType.PUBLIC);

        // Setup test create request
        testCreateRequest = new CreateMemoryRequest();
        testCreateRequest.setMediaUrl("https://cloudinary.com/test.jpg");
        testCreateRequest.setCloudinaryPublicId("test/image123");
        testCreateRequest.setMediaType(MediaType.IMAGE);
        testCreateRequest.setDescription("Test description");
        testCreateRequest.setLatitude(1.0);
        testCreateRequest.setLongitude(1.0);
        testCreateRequest.setVisibility(VisibilityType.PUBLIC);
    }

    @Test
    void testCreateMemory_Success() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(memoryRepository.save(any(Memory.class))).thenReturn(testMemory);
        when(dtoConverter.toDto(any(Memory.class))).thenReturn(testMemoryDto);

        // Act
        MemoryDto result = memoryService.createMemory(testCreateRequest, "testuser");

        // Assert
        assertNotNull(result);
        assertEquals("memoryId", result.getId());
        assertEquals("https://cloudinary.com/test.jpg", result.getMediaUrl());
        assertEquals("Test description", result.getDescription());
        assertEquals(VisibilityType.PUBLIC, result.getVisibility());
        
        verify(userRepository).findByUsername("testuser");
        verify(memoryRepository).save(any(Memory.class));
        verify(dtoConverter).toDto(any(Memory.class));
    }

    @Test
    void testCreateMemory_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> 
            memoryService.createMemory(testCreateRequest, "nonexistent")
        );
        
        verify(userRepository).findByUsername("nonexistent");
        verify(memoryRepository, never()).save(any(Memory.class));
    }

    @Test
    void testGetMemoryById() {
        // Arrange
        when(memoryRepository.findById("memoryId")).thenReturn(Optional.of(testMemory));
        when(dtoConverter.toDto(testMemory)).thenReturn(testMemoryDto);

        // Act
        MemoryDto result = memoryService.getMemoryById("memoryId");

        // Assert
        assertNotNull(result);
        assertEquals("memoryId", result.getId());
        assertEquals("https://cloudinary.com/test.jpg", result.getMediaUrl());
        assertEquals("Test description", result.getDescription());
        assertEquals(VisibilityType.PUBLIC, result.getVisibility());

        verify(memoryRepository).findById("memoryId");
        verify(dtoConverter).toDto(testMemory);
    }

    @Test
    void testGetMemoryById_NotFound() {
        // Arrange
        when(memoryRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> 
            memoryService.getMemoryById("nonexistent")
        );

        verify(memoryRepository).findById("nonexistent");
    }
}
