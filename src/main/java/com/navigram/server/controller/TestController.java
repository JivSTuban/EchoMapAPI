package com.navigram.server.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.transaction.annotation.Transactional;

import com.navigram.server.dto.MemoryDto;
import com.navigram.server.model.Memory;
import com.navigram.server.model.MediaType;
import com.navigram.server.model.VisibilityType;
import com.navigram.server.model.User;
import com.navigram.server.repository.MemoryRepository;
import com.navigram.server.repository.UserRepository;
import com.navigram.server.service.MemoryService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/test")
public class TestController {
    private static final Logger logger = LoggerFactory.getLogger(TestController.class);
    
    @Autowired
    private MemoryRepository memoryRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MemoryService memoryService;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @GetMapping("/hello")
    public ResponseEntity<Map<String, Object>> helloWorld() {
        logger.info("Test endpoint called");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello, World!");
        response.put("status", "success");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "running");
        status.put("timestamp", LocalDateTime.now().toString());
        status.put("memoryCount", memoryRepository.count());
        status.put("userCount", userRepository.count());
        return ResponseEntity.ok(status);
    }
    
    @PostMapping("/create-test-memory")
    @Transactional
    public ResponseEntity<Memory> createTestMemory(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam String description,
            @RequestParam String visibility) {
        
        // Create a test memory
        Memory memory = new Memory();
        memory.setId(UUID.randomUUID().toString());
        memory.setDescription(description);
        memory.setLatitude(lat);
        memory.setLongitude(lng);
        memory.setVisibility(VisibilityType.valueOf(visibility));
        memory.setCreatedAt(LocalDateTime.now());
        
        // Create a Point geometry for the location
        Point point = geometryFactory.createPoint(new Coordinate(lng, lat));
        memory.setLocation(point);
        
        // Get or create a test user
        User user = userRepository.findByUsername("testuser")
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setId("1");
                    newUser.setUsername("testuser");
                    newUser.setEmail("test@example.com");
                    newUser.setPassword("password123");
                    
                    logger.info("Setting role to: {}", com.navigram.server.model.Role.USER);
                    newUser.setRole(com.navigram.server.model.Role.USER);
                    
                    logger.info("User role after setting: {}", newUser.getRole());
                    newUser.setCreatedAt(LocalDateTime.now());
                    newUser.setSocialLogin(false);
                    newUser.setPhoneVerified(false);
                    return userRepository.save(newUser);
                });
        
        memory.setUser(user);
        
        // Save the memory
        Memory savedMemory = memoryRepository.save(memory);
        
        return ResponseEntity.ok(savedMemory);
    }
    
    @PostMapping("/create-simple-memory")
    @Transactional
    public ResponseEntity<?> createSimpleMemory(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam String description) {
        try {
            logger.info("Creating simple memory at lat={}, lng={} with description: {}", lat, lng, description);
            
            // First create a test user if it doesn't exist
            User user = userRepository.findByUsername("testuser")
                .orElseGet(() -> {
                    logger.info("Creating test user");
                    User newUser = new User();
                    newUser.setId(UUID.randomUUID().toString());
                    newUser.setUsername("testuser");
                    newUser.setEmail("test@example.com");
                    newUser.setPassword("password123");
                    newUser.setRole(com.navigram.server.model.Role.USER);
                    newUser.setCreatedAt(LocalDateTime.now());
                    newUser.setSocialLogin(false);
                    newUser.setPhoneVerified(false);
                    
                    try {
                        return userRepository.save(newUser);
                    } catch (Exception e) {
                        logger.error("Error saving test user: {}", e.getMessage(), e);
                        throw e;
                    }
                });
            
            // Create a test memory
            Memory memory = new Memory();
            memory.setId(UUID.randomUUID().toString());
            memory.setDescription(description);
            memory.setLatitude(lat);
            memory.setLongitude(lng);
            memory.setVisibility(VisibilityType.PUBLIC);
            memory.setCreatedAt(LocalDateTime.now());
            memory.setMediaType(MediaType.IMAGE);
            
            // Create a Point geometry for the location
            Point point = geometryFactory.createPoint(new Coordinate(lng, lat));
            memory.setLocation(point);
            
            // Set user
            memory.setUser(user);
            
            // Save the memory
            Memory savedMemory = memoryRepository.save(memory);
            logger.info("Successfully created memory with ID: {}", savedMemory.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("id", savedMemory.getId());
            response.put("message", "Memory created successfully");
            response.put("data", savedMemory);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error creating simple memory: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to create memory: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/memories")
    public ResponseEntity<List<MemoryDto>> getAllMemories() {
        return ResponseEntity.ok(memoryService.getAllPublicMemories());
    }
    
    @GetMapping("/memories/public")
    public ResponseEntity<List<MemoryDto>> getAllPublicMemories() {
        logger.info("Fetching all public memories");
        List<MemoryDto> memories = memoryService.getAllPublicMemories();
        logger.info("Found {} public memories", memories.size());
        return ResponseEntity.ok(memories);
    }
} 