package com.echomap.server.controller;

import com.echomap.server.dto.CreateMemoryRequest;
import com.echomap.server.dto.MemoryDto;
import com.echomap.server.service.MemoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/memories")
@CrossOrigin(origins = "*")
public class MemoryController {
    private final MemoryService memoryService;

    public MemoryController(MemoryService memoryService) {
        this.memoryService = memoryService;
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
        return new ResponseEntity<>(memoryService.createMemory(request, username), HttpStatus.CREATED);
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
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "10") double radius) {
        return ResponseEntity.ok(memoryService.getNearbyPublicMemories(latitude, longitude, radius));
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
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
    }
}
