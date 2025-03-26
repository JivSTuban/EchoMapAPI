package com.navigram.server.service;

import com.navigram.server.dto.CreateMemoryRequest;
import com.navigram.server.dto.MemoryDto;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

public interface MemoryService {
    List<MemoryDto> getAllMemories();
    MemoryDto createMemory(CreateMemoryRequest request, String username);
    MemoryDto getMemoryById(String id);
    List<MemoryDto> getNearbyMemories(double lat, double lng, double radius, long userId);
    List<MemoryDto> getNearbyPublicMemories(double lat, double lng, double radius);
    List<MemoryDto> getAllPublicMemories();
    MemoryDto updateMemory(String id, MemoryDto memoryDto);
    void deleteMemory(String id);

    @Transactional(readOnly = true)
    long getTotalMemories();

    @Transactional(readOnly = true)
    long getNewMemoriesToday();

    @Transactional(readOnly = true)
    long getApiRequestsToday();

    @Transactional(readOnly = true)
    String getServerUptime();

    @Transactional(readOnly = true)
    List<MemoryDto> getFlaggedMemories();

    @Transactional(readOnly = true)
    long getFlaggedMemoriesCount();

    @Transactional
    void approveMemory(String memoryId);
}
