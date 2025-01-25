package com.echomap.server.service;

import com.echomap.server.dto.UserDto;
import com.echomap.server.exception.ResourceNotFoundException;
import com.echomap.server.model.Role;
import com.echomap.server.model.User;
import com.echomap.server.repository.UserRepository;
import com.echomap.server.util.DtoConverter;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final DtoConverter dtoConverter;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, DtoConverter dtoConverter, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.dtoConverter = dtoConverter;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    @Transactional
    public UserDto createUser(UserDto userDto) {
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = dtoConverter.toEntity(userDto);
        // Encode password before saving
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        User savedUser = userRepository.save(user);
        return dtoConverter.toDto(savedUser);
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
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
    public UserDto updateUser(Long id, UserDto userDto) {
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
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }

    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public void followUser(Long id) {
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

    public void unfollowUser(Long id) {
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
