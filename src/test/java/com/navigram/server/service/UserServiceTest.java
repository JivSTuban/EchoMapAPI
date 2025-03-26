package com.navigram.server.service;

import com.navigram.server.dto.GuestAuthResult;
import com.navigram.server.dto.UserDto;
import com.navigram.server.model.Role;
import com.navigram.server.model.User;
import com.navigram.server.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    void findOrCreateSocialUser_ExistingUser() {
        User socialUser = new User();
        socialUser.setUsername("socialUser");
        socialUser.setEmail("social@example.com");
        socialUser.setRole(Role.USER);
        socialUser.setSocialLogin(true);

        UserDto socialUserDto = new UserDto();
        socialUserDto.setUsername("socialUser");
        socialUserDto.setEmail("social@example.com");

        when(userRepository.findByEmail("social@example.com")).thenReturn(Optional.of(socialUser));

        UserDto result = userService.findOrCreateSocialUser(socialUserDto);

        assertEquals("socialUser", result.getUsername());
        assertEquals("social@example.com", result.getEmail());
        verify(userRepository, never()).save(any());
    }

    @Test
    void findOrCreateSocialUser_NewUser() {
        UserDto newSocialUserDto = new UserDto();
        newSocialUserDto.setUsername("newSocialUser");
        newSocialUserDto.setEmail("new.social@example.com");
        newSocialUserDto.setPassword("password");

        User savedUser = new User();
        savedUser.setUsername("newSocialUser");
        savedUser.setEmail("new.social@example.com");
        savedUser.setRole(Role.USER);
        savedUser.setSocialLogin(true);

        when(userRepository.findByEmail("new.social@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");

        UserDto result = userService.findOrCreateSocialUser(newSocialUserDto);

        assertEquals("newSocialUser", result.getUsername());
        assertEquals("new.social@example.com", result.getEmail());
        assertEquals(Role.USER, result.getRole());
        verify(userRepository).save(any());
    }

    @Test
    void createGuestUser_Success() {
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("guest-id");
            return user;
        });

        GuestAuthResult result = userService.createGuestUser();

        assertNotNull(result.getUser());
        assertNotNull(result.getPassword());
        assertNotNull(result.getToken());
        assertTrue(result.getUser().getUsername().startsWith("guest_"));
        assertEquals(Role.GUEST, result.getUser().getRole());
        verify(userRepository).save(any());
    }

    @Test
    void createUser_Success() {
        UserDto userDto = new UserDto();
        userDto.setUsername("testUser");
        userDto.setEmail("test@example.com");
        userDto.setPassword("password");

        User savedUser = new User();
        savedUser.setUsername("testUser");
        savedUser.setEmail("test@example.com");
        savedUser.setRole(Role.USER);

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");

        UserDto result = userService.createUser(userDto);

        assertEquals("testUser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals(Role.USER, result.getRole());
        verify(userRepository).save(any());
    }
}
