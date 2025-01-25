package com.echomap.server.service;

import com.echomap.server.dto.MemoryDto;
import com.echomap.server.model.Memory;
import com.echomap.server.model.User;
import com.echomap.server.model.VisibilityType;
import com.echomap.server.repository.MemoryRepository;
import com.echomap.server.repository.UserRepository;
import com.echomap.server.util.DtoConverter;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemoryService {
    private final MemoryRepository memoryRepository;
    private final UserRepository userRepository;
    private final DtoConverter dtoConverter;

    public MemoryService(MemoryRepository memoryRepository, UserRepository userRepository, DtoConverter dtoConverter) {
        this.memoryRepository = memoryRepository;
        this.userRepository = userRepository;
        this.dtoConverter = dtoConverter;
    }

    @Transactional
    public MemoryDto createMemory(MemoryDto memoryDto) {
        User user = userRepository.findById(memoryDto.getUserId())
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Memory memory = dtoConverter.toEntity(memoryDto);
        memory.setUser(user);
        Memory savedMemory = memoryRepository.save(memory);
        return dtoConverter.toDto(savedMemory);
    }

    @Transactional(readOnly = true)
    public MemoryDto getMemoryById(String id) {
        Memory memory = memoryRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Memory not found"));
        return dtoConverter.toDto(memory);
    }

    @Transactional(readOnly = true)
    public List<MemoryDto> getNearbyMemories(double lat, double lng, double radius, Long userId) {
        List<Memory> memories = memoryRepository.findNearbyMemories(lat, lng, radius, userId);
        return memories.stream()
            .map(memory -> {
                MemoryDto dto = dtoConverter.toDto(memory);
                // Calculate distance for each memory
                dto.setDistanceInMeters(calculateDistance(lat, lng,
                    memory.getLocation().getY(), memory.getLocation().getX()));
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MemoryDto> getNearbyPublicMemories(double lat, double lng, double radius) {
        List<Memory> memories = memoryRepository.findNearbyPublicMemories(lat, lng, radius);
        return memories.stream()
            .map(memory -> {
                MemoryDto dto = dtoConverter.toDto(memory);
                dto.setDistanceInMeters(calculateDistance(lat, lng,
                    memory.getLocation().getY(), memory.getLocation().getX()));
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Transactional
    public MemoryDto updateMemory(String id, MemoryDto memoryDto) {
        Memory memory = memoryRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Memory not found"));

        memory.setVisibility(memoryDto.getVisibility());
        memory.setAudioUrl(memoryDto.getAudioUrl());
        memory.setLocation(dtoConverter.toEntity(memoryDto).getLocation());

        Memory updatedMemory = memoryRepository.save(memory);
        return dtoConverter.toDto(updatedMemory);
    }

    @Transactional
    public void deleteMemory(String id) {
        Memory memory = memoryRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Memory not found"));

        memoryRepository.delete(memory);
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth's radius in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Convert to meters
        return R * c * 1000;
    }
}
