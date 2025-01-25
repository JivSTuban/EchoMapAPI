package com.echomap.server.service;

import com.echomap.server.dto.UserDto;
import com.echomap.server.model.Role;
import com.echomap.server.model.User;
import com.echomap.server.repository.UserRepository;
import com.echomap.server.util.DtoConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
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
    private DtoConverter dtoConverter;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateUser() {
        UserDto userDto = new UserDto();
        userDto.setUsername("testuser");
        userDto.setEmail("testuser@example.com");
        userDto.setPassword("password");

        User user = new User();
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);

        when(userRepository.existsByUsername(any(String.class))).thenReturn(false);
        when(userRepository.existsByEmail(any(String.class))).thenReturn(false);
        when(dtoConverter.toEntity(any(UserDto.class))).thenReturn(user);
        when(passwordEncoder.encode(any(String.class))).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(dtoConverter.toDto(any(User.class))).thenReturn(userDto);

        UserDto result = userService.createUser(userDto);

        verify(userRepository, times(1)).existsByUsername(any(String.class));
        verify(userRepository, times(1)).existsByEmail(any(String.class));
        verify(dtoConverter, times(1)).toEntity(any(UserDto.class));
        verify(passwordEncoder, times(1)).encode(any(String.class));
        verify(userRepository, times(1)).save(any(User.class));
        verify(dtoConverter, times(1)).toDto(any(User.class));
    }

    @Test
    void testGetUserById() {
        String id = "1";
        User user = new User();
        user.setId(id);
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);

        UserDto userDto = new UserDto();
        userDto.setId(id);
        userDto.setUsername("testuser");
        userDto.setEmail("testuser@example.com");

        when(userRepository.findById(any(String.class))).thenReturn(Optional.of(user));
        when(dtoConverter.toDto(any(User.class))).thenReturn(userDto);

        UserDto result = userService.getUserById(id);

        verify(userRepository, times(1)).findById(any(String.class));
        verify(dtoConverter, times(1)).toDto(any(User.class));
    }

    @Test
    void testGetUserByUsername() {
        String username = "testuser";
        User user = new User();
        user.setUsername(username);
        user.setEmail("testuser@example.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);

        UserDto userDto = new UserDto();
        userDto.setUsername(username);
        userDto.setEmail("testuser@example.com");

        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.of(user));
        when(dtoConverter.toDto(any(User.class))).thenReturn(userDto);

        UserDto result = userService.getUserByUsername(username);

        verify(userRepository, times(1)).findByUsername(any(String.class));
        verify(dtoConverter, times(1)).toDto(any(User.class));
    }

    @Test
    void testUpdateUser() {
        String id = "1";
        UserDto userDto = new UserDto();
        userDto.setUsername("updateduser");
        userDto.setEmail("updateduser@example.com");
        userDto.setPassword("newpassword");

        User user = new User();
        user.setId(id);
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);

        when(userRepository.findById(any(String.class))).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername(any(String.class))).thenReturn(false);
        when(userRepository.existsByEmail(any(String.class))).thenReturn(false);
        when(passwordEncoder.encode(any(String.class))).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(dtoConverter.toDto(any(User.class))).thenReturn(userDto);

        UserDto result = userService.updateUser(id, userDto);

        verify(userRepository, times(1)).findById(any(String.class));
        verify(userRepository, times(1)).existsByUsername(any(String.class));
        verify(userRepository, times(1)).existsByEmail(any(String.class));
        verify(passwordEncoder, times(1)).encode(any(String.class));
        verify(userRepository, times(1)).save(any(User.class));
        verify(dtoConverter, times(1)).toDto(any(User.class));
    }

    @Test
    void testDeleteUser() {
        String id = "1";

        when(userRepository.existsById(any(String.class))).thenReturn(true);

        userService.deleteUser(id);

        verify(userRepository, times(1)).existsById(any(String.class));
        verify(userRepository, times(1)).deleteById(any(String.class));
    }

    @Test
    void testRegisterUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setPassword("password");
        user.setRole(Role.USER);

        when(passwordEncoder.encode(any(String.class))).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.registerUser(user);

        verify(passwordEncoder, times(1)).encode(any(String.class));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testCreateGuestUser() {
        User user = new User();
        user.setUsername("guestuser");
        user.setEmail("guestuser@example.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.GUEST);

        when(userRepository.save(any(User.class))).thenReturn(user);

        UserService.GuestAuthResult result = userService.createGuestUser();

        verify(userRepository, times(1)).save(any(User.class));
    }
}
