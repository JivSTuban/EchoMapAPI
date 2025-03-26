package com.navigram.server.controller;

import com.navigram.server.dto.CreateMemoryRequest;
import com.navigram.server.dto.MemoryDto;
import com.navigram.server.service.MemoryService;
import com.navigram.server.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

@RestController
@RequestMapping("/api/memories")
@CrossOrigin(origins = "*")
public class MemoryController {
    private final MemoryService memoryService;
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(MemoryController.class);

    public MemoryController(MemoryService memoryService, UserService userService) {
        this.memoryService = memoryService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<MemoryDto>> getAllMemories(Authentication authentication) {
        return ResponseEntity.ok(memoryService.getAllMemories());
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MemoryDto> createMemory(
            @Valid @RequestBody CreateMemoryRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        logger.info("Creating memory for user: {}", username);
        
        try {
            MemoryDto createdMemory = memoryService.createMemory(request, username);
            return new ResponseEntity<>(createdMemory, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error creating memory for user: {}", username, e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MemoryDto> getMemory(@PathVariable String id) {
        return ResponseEntity.ok(memoryService.getMemoryById(id));
    }

    @GetMapping("/nearby")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<MemoryDto>> getNearbyMemories(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "10") double radius,
            @RequestParam long userId) {
        return ResponseEntity.ok(memoryService.getNearbyMemories(latitude, longitude, radius, userId));
    }

    @GetMapping("/nearby/public")
    public ResponseEntity<List<MemoryDto>> getNearbyPublicMemories(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "1000") double radius) {
        try {
            logger.info("Fetching nearby public memories at lat={}, lng={}, radius={}", lat, lng, radius);
            List<MemoryDto> memories = memoryService.getNearbyPublicMemories(lat, lng, radius);
            
            // Filter memories manually by distance
            List<MemoryDto> filteredMemories = memories.stream()
                .filter(memory -> {
                    if (memory.getLatitude() == null || memory.getLongitude() == null) {
                        return false;
                    }
                    
                    // Calculate distance using Haversine formula
                    double distance = calculateDistance(
                        lat, lng,
                        memory.getLatitude(), memory.getLongitude()
                    );
                    
                    // Add distance to memory object for UI display
                    memory.setDistanceInMeters(distance * 1000);
                    
                    // Keep only memories within radius (in km)
                    return distance <= radius;
                })
                .collect(Collectors.toList());
            
            logger.info("Found {} memories within {}km", filteredMemories.size(), radius);
            return ResponseEntity.ok(filteredMemories);
        } catch (Exception e) {
            logger.error("Error fetching nearby public memories: {}", e.getMessage(), e);
            // Return empty list instead of error response
            return ResponseEntity.ok(List.of());
        }
    }
    
    // Calculate distance between two coordinates in km using Haversine formula
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth's radius in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    @GetMapping("/public/all")
    public ResponseEntity<List<MemoryDto>> getAllPublicMemories() {
        List<MemoryDto> memories = memoryService.getAllPublicMemories();
        return ResponseEntity.ok(memories);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') and (authentication.principal.username == @memoryService.getMemoryById(#id).user.username or hasRole('ADMIN'))")
    public ResponseEntity<MemoryDto> updateMemory(
            @PathVariable String id,
            @Valid @RequestBody MemoryDto memoryDto) {
        return ResponseEntity.ok(memoryService.updateMemory(id, memoryDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') and (authentication.principal.username == @memoryService.getMemoryById(#id).user.username or hasRole('ADMIN'))")
    public ResponseEntity<Map<String, String>> deleteMemory(@PathVariable String id) {
        memoryService.deleteMemory(id);
        return ResponseEntity.ok(Map.of("message", "Memory deleted successfully"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
