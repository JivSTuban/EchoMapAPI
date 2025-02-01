package com.echomap.server.service;

import com.echomap.server.dto.UserDto;
import com.echomap.server.model.Role;
import com.echomap.server.model.User;
import com.echomap.server.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setEmail(userDto.getEmail());
        user.setRole(Role.USER);

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
            return convertToDto(existingUser.get());
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
    public UserDto updateUser(String id, UserDto userDto) {
        logger.info("Updating user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        if (userDto.getUsername() != null) {
            user.setUsername(userDto.getUsername());
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        User savedUser = userRepository.save(user);
        logger.info("User updated successfully: {}", savedUser.getUsername());
        return convertToDto(savedUser);
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

    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        return dto;
    }

    public static class GuestAuthResult {
        private final String token;
        private final User user;
        private final String password;

        public GuestAuthResult(String token, User user, String password) {
            this.token = token;
            this.user = user;
            this.password = password;
        }

        public String getToken() {
            return token;
        }

        public User getUser() {
            return user;
        }

        public String getPassword() {
            return password;
        }
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
}
