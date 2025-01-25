package com.echomap.server.service;

import com.echomap.server.dto.FlagDto;
import com.echomap.server.model.Flag;
import com.echomap.server.model.Memory;
import com.echomap.server.model.VisibilityType;
import com.echomap.server.repository.FlagRepository;
import com.echomap.server.repository.MemoryRepository;
import com.echomap.server.util.DtoConverter;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FlagService {
    private static final int MAX_FLAGS_PER_MEMORY = 5;

    private final FlagRepository flagRepository;
    private final MemoryRepository memoryRepository;
    private final DtoConverter dtoConverter;

    public FlagService(FlagRepository flagRepository, MemoryRepository memoryRepository, DtoConverter dtoConverter) {
        this.flagRepository = flagRepository;
        this.memoryRepository = memoryRepository;
        this.dtoConverter = dtoConverter;
    }

    @Transactional
    public FlagDto createFlag(FlagDto flagDto) {
        Memory memory = memoryRepository.findById(flagDto.getMemoryId())
            .orElseThrow(() -> new EntityNotFoundException("Memory not found"));

        // Check if memory has reached maximum flags
        long flagCount = flagRepository.countByMemory(memory);
        if (flagCount >= MAX_FLAGS_PER_MEMORY) {
            // If max flags reached, memory should be hidden or reviewed
            memory.setVisibility(VisibilityType.PRIVATE);
            memoryRepository.save(memory);
            throw new IllegalStateException("Memory has been flagged too many times and is now hidden");
        }

        Flag flag = dtoConverter.toEntity(flagDto);
        memory.addFlag(flag);  // This will handle both sides of the relationship
        Flag savedFlag = flagRepository.save(flag);
        memoryRepository.save(memory);
        return dtoConverter.toDto(savedFlag);
    }

    @Transactional(readOnly = true)
    public List<FlagDto> getFlagsForMemory(String memoryId) {
        Memory memory = memoryRepository.findById(memoryId)
            .orElseThrow(() -> new EntityNotFoundException("Memory not found"));

        List<Flag> flags = flagRepository.findByMemory(memory);
        return flags.stream()
            .map(dtoConverter::toDto)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean isMemoryFlagged(String memoryId) {
        Memory memory = memoryRepository.findById(memoryId)
            .orElseThrow(() -> new EntityNotFoundException("Memory not found"));
        return flagRepository.existsByMemory(memory);
    }

    @Transactional
    public void resolveFlag(String id) {
        Flag flag = flagRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Flag not found"));
        flag.setResolved(true);
        flagRepository.save(flag);
    }

    @Transactional
    public void hideMemory(String id) {
        Memory memory = memoryRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Memory not found"));
        memory.setVisibility(VisibilityType.PRIVATE);
        memoryRepository.save(memory);
    }
}
