package com.navigram.server.service;

import com.navigram.server.dto.FlagDto;
import com.navigram.server.model.Flag;
import com.navigram.server.model.Memory;
import com.navigram.server.model.User;
import com.navigram.server.model.VisibilityType;
import com.navigram.server.repository.FlagRepository;
import com.navigram.server.repository.MemoryRepository;
import com.navigram.server.repository.UserRepository;
import com.navigram.server.util.DtoConverter;
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
    private final UserRepository userRepository;
    private final DtoConverter dtoConverter;

    public FlagService(FlagRepository flagRepository, MemoryRepository memoryRepository, UserRepository userRepository, DtoConverter dtoConverter) {
        this.flagRepository = flagRepository;
        this.memoryRepository = memoryRepository;
        this.userRepository = userRepository;
        this.dtoConverter = dtoConverter;
    }

    @Transactional(readOnly = true)
    public List<FlagDto> getAllFlags() {
        List<Flag> flags = flagRepository.findAll();
        return flags.stream()
            .map(dtoConverter::toDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public FlagDto createFlag(FlagDto flagDto, String reporterUsername) {
        Memory memory = memoryRepository.findById(flagDto.getMemoryId())
            .orElseThrow(() -> new EntityNotFoundException("Memory not found"));

        User reporter = userRepository.findByUsername(reporterUsername)
            .orElseThrow(() -> new EntityNotFoundException("Reporter not found"));

        // Check if memory has reached maximum flags
        long flagCount = flagRepository.countByMemory(memory);
        if (flagCount >= MAX_FLAGS_PER_MEMORY) {
            // If max flags reached, memory should be hidden or reviewed
            memory.setVisibility(VisibilityType.PRIVATE);
            memory.setIsFlagged(true);
            memoryRepository.save(memory);
            throw new IllegalStateException("Memory has been flagged too many times and is now hidden");
        }

        Flag flag = dtoConverter.toEntity(flagDto);
        flag.setReporter(reporter);
        memory.addFlag(flag);  // This will handle both sides of the relationship
        memory.setIsFlagged(true);  // Set the isFlagged field
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

        // Check if there are any unresolved flags for this memory
        Memory memory = flag.getMemory();
        boolean hasUnresolvedFlags = flagRepository.existsByMemoryAndResolvedFalse(memory);
        if (!hasUnresolvedFlags) {
            memory.setIsFlagged(false);
            memoryRepository.save(memory);
        }
    }

    @Transactional
    public void hideMemory(String id) {
        Memory memory = memoryRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Memory not found"));
        memory.setVisibility(VisibilityType.PRIVATE);
        memoryRepository.save(memory);
    }

    @Transactional(readOnly = true)
    public long getTotalFlags() {
        return flagRepository.count();
    }
}
