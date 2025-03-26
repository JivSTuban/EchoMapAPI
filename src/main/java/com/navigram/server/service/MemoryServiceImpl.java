package com.navigram.server.service;

import com.navigram.server.dto.CreateMemoryRequest;
import com.navigram.server.dto.MemoryDto;
import com.navigram.server.model.Memory;
import com.navigram.server.model.User;
import com.navigram.server.model.VisibilityType;
import com.navigram.server.repository.MemoryRepository;
import com.navigram.server.repository.UserRepository;
import com.navigram.server.util.DtoConverter;
import jakarta.persistence.EntityNotFoundException;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemoryServiceImpl implements MemoryService{

    private static final Logger logger = LoggerFactory.getLogger(MemoryServiceImpl.class);
    
    private final MemoryRepository memoryRepository;
    private final UserRepository userRepository;
    private final DtoConverter dtoConverter;
    private final GeometryFactory geometryFactory;

    public MemoryServiceImpl(
            MemoryRepository memoryRepository,
            UserRepository userRepository,
            DtoConverter dtoConverter) {
        this.memoryRepository = memoryRepository;
        this.userRepository = userRepository;
        this.dtoConverter = dtoConverter;
        this.geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemoryDto> getAllMemories() {
        try {
            logger.info("Retrieving all memories from database");
            List<Memory> memories = memoryRepository.findAll();
            logger.info("Successfully retrieved {} memories from database", memories.size());
            
            List<MemoryDto> memoryDtos = new ArrayList<>();
            for (Memory memory : memories) {
                try {
                    MemoryDto dto = dtoConverter.toDto(memory);
                    memoryDtos.add(dto);
                } catch (Exception e) {
                    logger.error("Error converting memory with ID {} to DTO: {}", memory.getId(), e.getMessage(), e);
                    // Continue with next memory instead of failing the entire request
                }
            }
            
            logger.info("Successfully converted {} memories to DTOs", memoryDtos.size());
            return memoryDtos;
        } catch (Exception e) {
            logger.error("Error fetching all memories: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve memories: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public MemoryDto createMemory(CreateMemoryRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Memory memory = new Memory();
        memory.setUser(user);
        memory.setMediaUrl(request.getMediaUrl());
        memory.setCloudinaryPublicId(request.getCloudinaryPublicId());
        memory.setMediaType(request.getMediaType());
        memory.setTitle(request.getTitle());
        memory.setDescription(request.getDescription());
        
        // Set both explicit lat/long fields and the Point object
        memory.setLatitude(request.getLatitude());
        memory.setLongitude(request.getLongitude());
        memory.setLocation(geometryFactory.createPoint(
                new Coordinate(request.getLongitude(), request.getLatitude())));
        
        memory.setVisibility(request.getVisibility());

        Memory savedMemory = memoryRepository.save(memory);
        return dtoConverter.toDto(savedMemory);
    }

    @Override
    @Transactional(readOnly = true)
    public MemoryDto getMemoryById(String id) {
        Memory memory = memoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Memory not found"));
        return dtoConverter.toDto(memory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemoryDto> getNearbyMemories(double lat, double lng, double radius, long userId) {
        List<Memory> memories = memoryRepository.findNearbyMemories(lat, lng, radius, userId);
        return memories.stream()
                .map(memory -> {
                    MemoryDto dto = dtoConverter.toDto(memory);
                    dto.setDistanceInMeters(calculateDistance(lat, lng,
                            memory.getLocation().getY(), memory.getLocation().getX()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemoryDto> getNearbyPublicMemories(double lat, double lng, double radius) {
        try {
            List<Memory> memories = memoryRepository.findNearbyPublicMemories(lat, lng, radius);
            return memories.stream()
                .map(memory -> {
                    MemoryDto dto = dtoConverter.toDto(memory);
                    double distanceInMeters = calculateHaversineDistance(
                        lat, lng,
                        memory.getLatitude(), memory.getLongitude()
                    );
                    dto.setDistanceInMeters(distanceInMeters);
                    return dto;
                })
                .sorted(Comparator.comparing(MemoryDto::getDistanceInMeters))
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching nearby public memories", e);
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional
    public MemoryDto updateMemory(String id, MemoryDto memoryDto) {
        Memory memory = memoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Memory not found"));

        memory.setVisibility(memoryDto.getVisibility());
        memory.setMediaUrl(memoryDto.getMediaUrl());
        memory.setTitle(memoryDto.getTitle());
        memory.setDescription(memoryDto.getDescription());
        if (memoryDto.getLatitude() != null && memoryDto.getLongitude() != null) {
            // Update both explicit fields and the Point
            memory.setLatitude(memoryDto.getLatitude());
            memory.setLongitude(memoryDto.getLongitude());
            memory.setLocation(geometryFactory.createPoint(
                    new Coordinate(memoryDto.getLongitude(), memoryDto.getLatitude())));
        }

        Memory updatedMemory = memoryRepository.save(memory);
        return dtoConverter.toDto(updatedMemory);
    }

    @Override
    @Transactional
    public void deleteMemory(String id) {
        Memory memory = memoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Memory not found"));

        memoryRepository.delete(memory);
    }

    @Override
    public List<MemoryDto> getAllPublicMemories() {
        try {
            List<Memory> memories = memoryRepository.findByVisibility(VisibilityType.PUBLIC);
            return memories.stream()
                .map(memory -> dtoConverter.toDto(memory))
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching all public memories", e);
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalMemories() {
        return memoryRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long getNewMemoriesToday() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        return memoryRepository.countByCreatedAtAfter(startOfDay);
    }

    @Override
    @Transactional(readOnly = true)
    public long getApiRequestsToday() {
        // This is a placeholder. In a real application, you would track API requests
        // using a separate counter or monitoring system
        return 0;
    }

    @Override
    @Transactional(readOnly = true)
    public String getServerUptime() {
        // This is a placeholder. In a real application, you would track server uptime
        // using a monitoring system
        return "24/7";
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemoryDto> getFlaggedMemories() {
        return memoryRepository.findByIsFlaggedTrue().stream()
                .map(dtoConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getFlaggedMemoriesCount() {
        return memoryRepository.countByIsFlaggedTrue();
    }

    @Override
    @Transactional
    public void approveMemory(String memoryId) {
        Memory memory = memoryRepository.findById(memoryId)
            .orElseThrow(() -> new EntityNotFoundException("Memory not found"));
        memory.setIsFlagged(false);
        memoryRepository.save(memory);
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

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
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
