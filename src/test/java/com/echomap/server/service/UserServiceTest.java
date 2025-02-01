package com.echomap.server.service;

import com.echomap.server.dto.UserDto;
import com.echomap.server.model.Role;
import com.echomap.server.model.User;
import com.echomap.server.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void whenCreateUser_thenReturnUserDto() {
        // Arrange
        UserDto userDto = new UserDto();
        userDto.setUsername("testUser");
        userDto.setEmail("test@example.com");
        userDto.setPassword("password");

        User savedUser = new User();
        savedUser.setId("1");
        savedUser.setUsername(userDto.getUsername());
        savedUser.setEmail(userDto.getEmail());
        savedUser.setPassword("encodedPassword");
        savedUser.setRole(Role.USER);

        when(userRepository.findByUsername(userDto.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(userDto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        UserDto result = userService.createUser(userDto);

        // Assert
        assertNotNull(result);
        assertEquals(userDto.getUsername(), result.getUsername());
        assertEquals(userDto.getEmail(), result.getEmail());
        assertEquals(Role.USER, result.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void whenCreateGuestUser_thenReturnGuestAuthResult() {
        // Arrange
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            // Ensure the saved user retains the properties set in createGuestUser
            assertNotNull(savedUser.getUsername());
            assertTrue(savedUser.getUsername().startsWith("guest_"));
            assertNotNull(savedUser.getEmail());
            assertTrue(savedUser.getEmail().endsWith("@temporary.echomap.com"));
            assertEquals(Role.GUEST, savedUser.getRole());
            return savedUser;
        });

        // Act
        UserService.GuestAuthResult result = userService.createGuestUser();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getToken());
        assertNotNull(result.getUser());
        assertEquals(Role.GUEST, result.getUser().getRole());
        assertTrue(result.getUser().getUsername().startsWith("guest_"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void whenFindOrCreateSocialUser_withExistingUser_thenReturnExistingUser() {
        // Arrange
        UserDto userDto = new UserDto();
        userDto.setEmail("social@example.com");
        userDto.setUsername("socialUser");

        User existingUser = new User();
        existingUser.setId("1");
        existingUser.setEmail(userDto.getEmail());
        existingUser.setUsername(userDto.getUsername());
        existingUser.setRole(Role.USER);
        existingUser.setSocialLogin(true);

        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(Optional.of(existingUser));

        // Act
        UserDto result = userService.findOrCreateSocialUser(userDto);

        // Assert
        assertNotNull(result);
        assertEquals(userDto.getEmail(), result.getEmail());
        assertEquals(userDto.getUsername(), result.getUsername());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void whenFindOrCreateSocialUser_withNewUser_thenCreateAndReturnNewUser() {
        // Arrange
        UserDto userDto = new UserDto();
        userDto.setEmail("new.social@example.com");
        userDto.setUsername("newSocialUser");
        userDto.setPassword("password");

        User newUser = new User();
        newUser.setId("2");
        newUser.setEmail(userDto.getEmail());
        newUser.setUsername(userDto.getUsername());
        newUser.setRole(Role.USER);
        newUser.setSocialLogin(true);

        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // Act
        UserDto result = userService.findOrCreateSocialUser(userDto);

        // Assert
        assertNotNull(result);
        assertEquals(userDto.getEmail(), result.getEmail());
        assertEquals(userDto.getUsername(), result.getUsername());
        verify(userRepository).save(any(User.class));
    }
}
