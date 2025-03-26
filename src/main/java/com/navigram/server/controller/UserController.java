package com.navigram.server.controller;

import com.navigram.server.dto.UserDto;
import com.navigram.server.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/{id}/profile")
    public ResponseEntity<String> getUserProfile(@PathVariable String id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(user.getProfilePicture());
    }

    @GetMapping("/{id}/ban-status")
    public ResponseEntity<Map<String, Object>> getUserBanStatus(@PathVariable String id) {
        UserDto user = userService.getUserById(id);
        Map<String, Object> response = new HashMap<>();
        
        if (!user.isEnabled()) {
            response.put("banned", true);
            response.put("banEndDate", user.getBanEndDate());
            response.put("banReason", "Account has been suspended by moderator or administrator");
        } else {
            response.put("banned", false);
        }
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable String id,
            @RequestBody UserDto userDto) {
        return ResponseEntity.ok(userService.updateUser(id, userDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/follow")
    public ResponseEntity<Void> followUser(
            @PathVariable String id,
            Authentication authentication) {
        String currentUserId = userService.getUserByUsername(authentication.getName()).getId();
        userService.followUser(currentUserId, id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/unfollow")
    public ResponseEntity<Void> unfollowUser(
            @PathVariable String id,
            Authentication authentication) {
        String currentUserId = userService.getUserByUsername(authentication.getName()).getId();
        userService.unfollowUser(currentUserId, id);
        return ResponseEntity.ok().build();
    }
}
