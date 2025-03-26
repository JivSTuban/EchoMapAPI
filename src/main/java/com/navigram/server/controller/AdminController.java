package com.navigram.server.controller;

import com.navigram.server.dto.UserDto;
import com.navigram.server.dto.MemoryDto;
import com.navigram.server.dto.FlagDto;
import com.navigram.server.model.Role;
import com.navigram.server.service.UserService;
import com.navigram.server.service.MemoryService;
import com.navigram.server.service.FlagService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {
    private final UserService userService;
    private final MemoryService memoryService;
    private final FlagService flagService;

    public AdminController(UserService userService, MemoryService memoryService, FlagService flagService) {
        this.userService = userService;
        this.memoryService = memoryService;
        this.flagService = flagService;
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) {
        return ResponseEntity.ok(userService.createUser(userDto));
    }

    @PutMapping("/users/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> updateUserRole(
            @PathVariable String userId,
            @RequestParam Role newRole) {
        return ResponseEntity.ok(userService.updateUserRole(userId, newRole));
    }

    @PutMapping("/users/{userId}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> suspendUser(@PathVariable String userId) {
        userService.suspendUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userService.getTotalUsers());
        stats.put("totalMemories", memoryService.getTotalMemories());
        stats.put("totalFlags", flagService.getTotalFlags());
        stats.put("activeUsers", userService.getActiveUsers());
        stats.put("newUsersToday", userService.getNewUsersToday());
        stats.put("newMemoriesToday", memoryService.getNewMemoriesToday());
        stats.put("flaggedMemories", memoryService.getFlaggedMemoriesCount());
        stats.put("activeSessions", userService.getActiveSessions());
        stats.put("apiRequestsToday", memoryService.getApiRequestsToday());
        stats.put("serverUptime", memoryService.getServerUptime());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/flagged-memories")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<List<MemoryDto>> getFlaggedMemories() {
        return ResponseEntity.ok(memoryService.getFlaggedMemories());
    }

    @PostMapping("/memories/{memoryId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<Void> approveMemory(@PathVariable String memoryId) {
        memoryService.approveMemory(memoryId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/memories/{memoryId}/delete")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<Void> deleteMemory(@PathVariable String memoryId) {
        memoryService.deleteMemory(memoryId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/memories/{memoryId}/flags")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<List<FlagDto>> getMemoryFlags(@PathVariable String memoryId) {
        return ResponseEntity.ok(flagService.getFlagsForMemory(memoryId));
    }

    @PostMapping("/users/{userId}/ban")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<Void> banUser(
            @PathVariable String userId,
            @RequestBody Map<String, Object> banRequest) {
        int duration = (int) banRequest.get("duration");
        String unit = (String) banRequest.get("unit");
        
        if ("permanent".equals(unit)) {
            userService.banUserPermanently(userId);
        } else {
            userService.banUser(userId, duration, unit);
        }
        
        return ResponseEntity.noContent().build();
    }
} 