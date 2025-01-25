package com.echomap.server.controller;

import com.echomap.server.dto.MemoryDto;
import com.echomap.server.service.MemoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/memories")
@CrossOrigin(origins = "*")
public class MemoryController {
    private final MemoryService memoryService;

    public MemoryController(MemoryService memoryService) {
        this.memoryService = memoryService;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MemoryDto> createMemory(@Valid @RequestBody MemoryDto memoryDto) {
        return new ResponseEntity<>(memoryService.createMemory(memoryDto), HttpStatus.CREATED);
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
    public ResponseEntity<Void> deleteMemory(@PathVariable String id) {
        memoryService.deleteMemory(id);
        return ResponseEntity.noContent().build();
    }
}
