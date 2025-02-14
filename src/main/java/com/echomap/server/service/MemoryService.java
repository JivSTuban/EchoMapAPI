package com.echomap.server.service;

import com.echomap.server.dto.CreateMemoryRequest;
import com.echomap.server.dto.MemoryDto;
import java.util.List;

public interface MemoryService {
    MemoryDto createMemory(CreateMemoryRequest request, String username);
    MemoryDto getMemoryById(String id);
    List<MemoryDto> getNearbyMemories(double lat, double lng, double radius, Long userId);
    List<MemoryDto> getNearbyPublicMemories(double lat, double lng, double radius);
    MemoryDto updateMemory(String id, MemoryDto memoryDto);
    void deleteMemory(String id);
}
