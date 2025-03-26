package com.navigram.server.util;

import com.navigram.server.dto.FlagDto;
import com.navigram.server.dto.MemoryDto;
import com.navigram.server.dto.UserDto;
import com.navigram.server.model.Flag;
import com.navigram.server.model.Memory;
import com.navigram.server.model.User;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;

@Component
public class DtoConverter {
    private final GeometryFactory geometryFactory;

    public DtoConverter() {
        // SRID 4326 is for WGS84 coordinate system (standard GPS coordinates)
        this.geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    }

    public User toEntity(UserDto dto) {
        System.out.println("Converting UserDto to User entity");
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setRole(dto.getRole());
        user.setEnabled(dto.isEnabled());
        System.out.println("User entity created: " + user);
        return user;
    }

    public UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setEnabled(user.isEnabled());
        dto.setName(user.getName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setPhoneVerified(user.isPhoneVerified());
        dto.setSocialLogin(user.isSocialLogin());
        dto.setProfilePicture(user.getProfilePicture());
        return dto;
    }

    public Memory toEntity(MemoryDto dto) {
        Memory memory = new Memory();
        memory.setMediaUrl(dto.getMediaUrl());
        memory.setMediaType(dto.getMediaType());
        memory.setTitle(dto.getTitle());
        memory.setDescription(dto.getDescription());
        memory.setAudioUrl(dto.getAudioUrl());
        memory.setVisibility(dto.getVisibility());
        memory.setLatitude(dto.getLatitude());
        memory.setLongitude(dto.getLongitude());

        // Convert lat/lng to Point
        Point point = geometryFactory.createPoint(
            new Coordinate(dto.getLongitude(), dto.getLatitude())
        );
        memory.setLocation(point);

        return memory;
    }

    public MemoryDto toDto(Memory memory) {
        if (memory == null) {
            return null;
        }

        MemoryDto dto = new MemoryDto();
        dto.setId(memory.getId());
        
        // Handle potential null user
        if (memory.getUser() != null) {
            dto.setUserId(memory.getUser().getId());
            dto.setUsername(memory.getUser().getUsername());
            dto.setName(memory.getUser().getName());
        }
        
        dto.setMediaUrl(memory.getMediaUrl());
        dto.setMediaType(memory.getMediaType());
        dto.setTitle(memory.getTitle());
        dto.setDescription(memory.getDescription());
        dto.setAudioUrl(memory.getAudioUrl());
        dto.setCreatedAt(memory.getCreatedAt());
        dto.setUpvoteCount(memory.getUpvoteCount());
        dto.setVisibility(memory.getVisibility());

        // Convert Point to lat/lng safely
        Point point = memory.getLocation();
        if (point != null) {
            dto.setLatitude(point.getY());  // Y coordinate is latitude
            dto.setLongitude(point.getX()); // X coordinate is longitude
        } else {
            // Fallback to the direct latitude/longitude fields
            dto.setLatitude(memory.getLatitude());
            dto.setLongitude(memory.getLongitude());
        }

        // Set flag reason if memory is flagged
        if (memory.isFlagged() && memory.getFlags() != null && !memory.getFlags().isEmpty()) {
            // Get the most recent unresolved flag
            String flagReason = memory.getFlags().stream()
                .filter(flag -> !flag.isResolved())
                .findFirst()
                .map(Flag::getReason)
                .orElse(null);
            dto.setFlagReason(flagReason);

            // Set total unresolved flags
            long totalUnresolvedFlags = memory.getFlags().stream()
                .filter(flag -> !flag.isResolved())
                .count();
            dto.setTotalFlags((int) totalUnresolvedFlags);
        }

        return dto;
    }

    public Flag toEntity(FlagDto dto) {
        Flag flag = new Flag();
        flag.setReason(dto.getReason());
        return flag;
    }

    public FlagDto toDto(Flag flag) {
        FlagDto dto = new FlagDto();
        dto.setId(flag.getId());
        dto.setMemoryId(flag.getMemory().getId());
        dto.setMemoryUserId(flag.getMemory().getUser().getId());
        dto.setReason(flag.getReason());
        dto.setCreatedAt(flag.getCreatedAt());
        dto.setReporterId(flag.getReporter().getId());
        dto.setReporterUsername(flag.getReporter().getUsername());
        return dto;
    }
}
