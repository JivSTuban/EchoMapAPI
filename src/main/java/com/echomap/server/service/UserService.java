package com.echomap.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.echomap.server.dto.UserDto;
import com.echomap.server.exception.ResourceNotFoundException;
import com.echomap.server.model.Role;
import com.echomap.server.model.User;
import com.echomap.server.repository.UserRepository;
import com.echomap.server.util.DtoConverter;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService implements UserDetailsService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final DtoConverter dtoConverter;
    private final PasswordEncoder passwordEncoder;

    // Cache for guest passwords (encoded)
    private static final Map<String, String> guestPasswordCache = new ConcurrentHashMap<>();
    private static final Map<String, GuestCredentials> guestCredentialsCache = new ConcurrentHashMap<>();

    @Autowired
    public UserService(UserRepository userRepository, DtoConverter dtoConverter, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.dtoConverter = dtoConverter;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // If this is a guest user and we have cached credentials
        GuestCredentials credentials = guestCredentialsCache.get(username);
        log.debug("Loading user details - Username: {}, Is Guest: {}", username, user.getRole() == Role.GUEST);
        log.debug("Credentials state - Cached Present: {}", credentials != null);

        if (user.getRole() == Role.GUEST && credentials != null) {
            log.debug("Using cached raw password for guest authentication");
            user.setPassword(credentials.rawPassword);
        }

        return user;
    }

    // Guest password management
    public void setGuestPassword(String username, String rawPassword) {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        guestPasswordCache.put(username, rawPassword);
        log.debug("Stored guest password for user: {}", username);
    }

    public String getGuestPassword(String username) {
        String password = guestPasswordCache.get(username);
        log.debug("Retrieved guest password for user: {}, exists: {}", username, password != null);
        return password;
    }

    public void clearGuestPassword(String username) {
        guestPasswordCache.remove(username);
        log.debug("Cleared guest password for user: {}", username);
    }

    @Transactional
    public UserDto createUser(UserDto userDto) {
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        System.out.println("Converting UserDto to User entity");
        User user = dtoConverter.toEntity(userDto);
        if (user == null) {
            throw new IllegalArgumentException("Failed to convert UserDto to User entity");
        }

        // Encode password before saving
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        User savedUser = userRepository.save(user);
        return dtoConverter.toDto(savedUser);
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(String id) {
        return userRepository.findById(id)
            .map(user -> dtoConverter.toDto(user))
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }

    @Transactional(readOnly = true)
    public UserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return dtoConverter.toDto(user);
    }

    @Transactional
    public UserDto updateUser(String id, UserDto userDto) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Check if new username is already taken by another user
        if (!user.getUsername().equals(userDto.getUsername()) &&
            userRepository.existsByUsername(userDto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if new email is already taken by another user
        if (!user.getEmail().equals(userDto.getEmail()) &&
            userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());

        // Update password if provided
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        return dtoConverter.toDto(updatedUser);
    }

    @Transactional
    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }

    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public void followUser(String id) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User userToFollow = userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!currentUser.getFollowing().contains(userToFollow)) {
            currentUser.getFollowing().add(userToFollow);
            userToFollow.getFollowers().add(currentUser);
            userRepository.save(currentUser);
            userRepository.save(userToFollow);
        }
    }

    public void unfollowUser(String id) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User userToUnfollow = userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (currentUser.getFollowing().contains(userToUnfollow)) {
            currentUser.getFollowing().remove(userToUnfollow);
            userToUnfollow.getFollowers().remove(currentUser);
            userRepository.save(currentUser);
            userRepository.save(userToUnfollow);
        }
    }

    public static class GuestAuthResult {
        private final User user;
        private final String password;

        public GuestAuthResult(User user, String password) {
            this.user = user;
            this.password = password;
        }

        public User getUser() { return user; }
        public String getPassword() { return password; }
    }

    @Transactional
    public GuestAuthResult createGuestUser() {
        String username = generateUniqueUsername();
        String rawPassword = generateRandomPassword();
        String email = generateUniqueEmail();

        log.info("Creating guest user with username: {} and email: {}", username, email);

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(Role.GUEST);

        User savedUser = userRepository.save(user);
        log.info("Guest user saved successfully");

        return new GuestAuthResult(savedUser, rawPassword);
    }

    private String generateUniqueEmail() {
        String email;
        do {
            email = "guest_" + UUID.randomUUID().toString().substring(0, 8) + "@navigram.com";
        } while (userRepository.existsByEmail(email));
        return email;
    }

    private String generateUniqueUsername() {
        String username;
        do {
            username = "guest_" + UUID.randomUUID().toString().substring(0, 8);
        } while (userRepository.existsByUsername(username));
        return username;
    }

    private String generateRandomPassword() {
        return UUID.randomUUID().toString().substring(0, 12);
    }

    public User createGuestUser(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        User guestUser = new User();
        guestUser.setUsername(username);
        guestUser.setPassword(passwordEncoder.encode(password));
        guestUser.setRole(Role.GUEST); // Assuming you have a GUEST role
        return userRepository.save(guestUser);
    }
}

class GuestCredentials {
    String rawPassword;

    public GuestCredentials(String rawPassword) {
        this.rawPassword = rawPassword;
    }
}
