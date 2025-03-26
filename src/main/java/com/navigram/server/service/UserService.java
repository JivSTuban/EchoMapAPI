package com.navigram.server.service;

import com.navigram.server.dto.UserDto;
import com.navigram.server.dto.GuestAuthResult;
import com.navigram.server.model.Role;
import com.navigram.server.model.User;
import com.navigram.server.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Loading user by username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Transactional
    public UserDto createUser(UserDto userDto) {
        logger.info("Creating user with username: {}", userDto.getUsername());
        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            logger.error("Username already exists: {}", userDto.getUsername());
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            logger.error("Email already exists: {}", userDto.getEmail());
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setEmail(userDto.getEmail());
        user.setRole(userDto.getRole() != null ? userDto.getRole() : Role.USER);
        user.setName(userDto.getName());
        
        if (userDto.getPhoneNumber() != null && !userDto.getPhoneNumber().isEmpty()) {
            user.setPhoneNumber(userDto.getPhoneNumber());
            user.setPhoneVerified(false);
        }

        User savedUser = userRepository.save(user);
        logger.info("User created successfully: {}", savedUser.getUsername());
        return convertToDto(savedUser);
    }

    @Transactional
    public UserDto findOrCreateSocialUser(UserDto userDto) {
        logger.info("Finding or creating social user with email: {}", userDto.getEmail());
        Optional<User> existingUser = userRepository.findByEmail(userDto.getEmail());

        if (existingUser.isPresent()) {
            logger.info("Existing user found: {}", existingUser.get().getUsername());
            User user = existingUser.get();
            // Only save if socialLogin status needs to be updated
            if (!user.isSocialLogin()) {
                user.setSocialLogin(true);
                userRepository.save(user);
                logger.info("Updated existing user to socialLogin: {}", user.getUsername());
            } else {
                logger.info("User already has socialLogin enabled, no update needed: {}", user.getUsername());
            }
            return convertToDto(user);
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setEmail(userDto.getEmail());
        user.setRole(Role.USER);
        user.setSocialLogin(true);

        User savedUser = userRepository.save(user);
        logger.info("Social user created successfully: {}", savedUser.getUsername());
        return convertToDto(savedUser);
    }

    public List<SimpleGrantedAuthority> getUserAuthorities(Role role) {
        logger.info("Getting authorities for role: {}", role);
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    public UserDto getUserByUsername(String username) {
        logger.info("Getting user by username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return convertToDto(user);
    }

    public UserDto getUserById(String id) {
        logger.info("Getting user by ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        return convertToDto(user);
    }

    @Transactional
    public void deleteUser(String id) {
        logger.info("Deleting user with ID: {}", id);
        userRepository.deleteById(id);
    }

    @Transactional
    public void followUser(String followerId, String followingId) {
        logger.info("Following user with ID: {} by user with ID: {}", followingId, followerId);
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new RuntimeException("Follower not found: " + followerId));
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new RuntimeException("User to follow not found: " + followingId));

        following.getFollowers().add(follower);
        userRepository.save(following);
    }

    @Transactional
    public void unfollowUser(String followerId, String followingId) {
        logger.info("Unfollowing user with ID: {} by user with ID: {}", followingId, followerId);
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new RuntimeException("Follower not found: " + followerId));
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new RuntimeException("User to unfollow not found: " + followingId));

        following.getFollowers().remove(follower);
        userRepository.save(following);
    }

    @Transactional
    public GuestAuthResult createGuestUser() {
        String guestId = UUID.randomUUID().toString().substring(0, 8);
        String guestUsername = "guest_" + guestId;
        String guestPassword = UUID.randomUUID().toString();
        String guestEmail = guestUsername + "@temporary.echomap.com";

        User guestUser = new User();
        guestUser.setId(UUID.randomUUID().toString());
        guestUser.setUsername(guestUsername);
        guestUser.setPassword(passwordEncoder.encode(guestPassword));
        guestUser.setEmail(guestEmail);
        guestUser.setRole(Role.GUEST);

        User savedUser = userRepository.save(guestUser);
        logger.info("Guest user created successfully: {}", savedUser.getUsername());
        return new GuestAuthResult(UUID.randomUUID().toString(), savedUser, guestPassword);
    }

    @Transactional
    public UserDto updateUser(String id, UserDto userDto) {
        logger.info("Updating user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        // Only update username for non-social login accounts
        if (userDto.getUsername() != null && !user.isSocialLogin()) {
            user.setUsername(userDto.getUsername());
        }
        
        // Only update email for non-social login accounts
        if (userDto.getEmail() != null && !user.isSocialLogin()) {
            user.setEmail(userDto.getEmail());
        }
        
        // Update name for all users
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
            logger.info("Updated name to: {}", userDto.getName());
        }
        
        // Update phone number for all users
        if (userDto.getPhoneNumber() != null) {
            user.setPhoneNumber(userDto.getPhoneNumber());
            logger.info("Updated phone number to: {}", userDto.getPhoneNumber());
        }
        
        // Update profile picture for all users
        if (userDto.getProfilePicture() != null) {
            user.setProfilePicture(userDto.getProfilePicture());
            logger.info("Updated profile picture");
        }
        
        // Only update password for non-social login accounts
        if (userDto.getPassword() != null && !user.isSocialLogin()) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        User savedUser = userRepository.save(user);
        logger.info("User updated successfully: {}", savedUser.getUsername());
        return convertToDto(savedUser);
    }

    @Transactional
    public boolean updatePhoneVerificationStatus(String userId, boolean verified) {
        logger.info("Updating phone verification status for user: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        user.setPhoneVerified(verified);
        userRepository.save(user);
        return true;
    }

    @Transactional
    public User createSocialUser(String username, String email, String name, String profilePicture, String provider, Role role) {
        logger.info("Creating social user with email: {}, provider: {}, role: {}", email, provider, role);
        
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(username);
        user.setEmail(email);
        user.setName(name != null ? name : username);
        user.setProfilePicture(profilePicture);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setRole(role);
        user.setSocialLogin(true);
        user.setSocialProvider(provider);
        
        User savedUser = userRepository.save(user);
        logger.info("Social user created successfully: {}", savedUser.getUsername());
        return savedUser;
    }

    public Optional<User> findByEmail(String email) {
        logger.info("Finding user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    public boolean existsByUsername(String username) {
        logger.info("Checking if username exists: {}", username);
        return userRepository.findByUsername(username).isPresent();
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public UserDto updateUserRole(String userId, Role newRole) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));
        user.setRole(newRole);
        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }

    @Transactional
    public void suspendUser(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public long getTotalUsers() {
        return userRepository.count();
    }

    @Transactional(readOnly = true)
    public long getActiveUsers() {
        return userRepository.count();
    }

    @Transactional(readOnly = true)
    public long getNewUsersToday() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        return userRepository.countByCreatedAtAfter(startOfDay);
    }

    @Transactional(readOnly = true)
    public long getActiveSessions() {
        // For now, we'll return the total number of active users
        // In a real application, you would track active sessions separately
        return getActiveUsers();
    }

    @Transactional
    public void banUser(String userId, int duration, String unit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        LocalDateTime banEndDate;
        switch (unit.toLowerCase()) {
            case "days":
                banEndDate = LocalDateTime.now().plusDays(duration);
                break;
            case "weeks":
                banEndDate = LocalDateTime.now().plusWeeks(duration);
                break;
            case "months":
                banEndDate = LocalDateTime.now().plusMonths(duration);
                break;
            default:
                throw new IllegalArgumentException("Invalid time unit. Use 'days', 'weeks', or 'months'");
        }

        user.setBanEndDate(banEndDate);
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Transactional
    public void banUserPermanently(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        user.setBanEndDate(null); // null indicates permanent ban
        user.setEnabled(false);
        userRepository.save(user);
    }

    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setRole(user.getRole());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setPhoneVerified(user.isPhoneVerified());
        dto.setSocialLogin(user.isSocialLogin());
        dto.setEnabled(user.isEnabled());
        dto.setProfilePicture(user.getProfilePicture());
        dto.setBanEndDate(user.getBanEndDate());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
