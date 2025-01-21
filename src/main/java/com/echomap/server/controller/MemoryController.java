package com.echomap.server.controller;

import com.echomap.server.dto.MemoryDto;
import com.echomap.server.service.MemoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<MemoryDto> createMemory(
            @Valid @RequestBody MemoryDto memoryDto,
            @RequestHeader("User-Id") String userId) {
        return new ResponseEntity<>(memoryService.createMemory(memoryDto, userId), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemoryDto> getMemory(@PathVariable String id) {
        return ResponseEntity.ok(memoryService.getMemoryById(id));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<MemoryDto>> getNearbyMemories(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "100") double radius,
            @RequestHeader("User-Id") String userId) {
        return ResponseEntity.ok(memoryService.getNearbyMemories(lat, lng, radius, userId));
    }

    @GetMapping("/nearby/public")
    public ResponseEntity<List<MemoryDto>> getNearbyPublicMemories(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "100") double radius) {
        return ResponseEntity.ok(memoryService.getNearbyPublicMemories(lat, lng, radius));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MemoryDto> updateMemory(
            @PathVariable String id,
            @Valid @RequestBody MemoryDto memoryDto,
            @RequestHeader("User-Id") String userId) {
        return ResponseEntity.ok(memoryService.updateMemory(id, memoryDto, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMemory(
            @PathVariable String id,
            @RequestHeader("User-Id") String userId) {
        memoryService.deleteMemory(id, userId);
        return ResponseEntity.noContent().build();
    }
}
