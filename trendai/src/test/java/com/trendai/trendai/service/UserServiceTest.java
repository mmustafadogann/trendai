package com.trendai.trendai.service;

import com.trendai.trendai.dto.CreateUserRequest;
import com.trendai.trendai.dto.UserResponse;
import com.trendai.trendai.entity.User;
import com.trendai.trendai.exception.BusinessException;
import com.trendai.trendai.mapper.UserMapper;
import com.trendai.trendai.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import com.trendai.trendai.exception.ResourceNotFoundException;
import java.util.Optional;
import com.trendai.trendai.dto.UpdateUserRequest;
import static org.mockito.Mockito.verify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void testCreateUser() {

        CreateUserRequest request = new CreateUserRequest();
        request.setFirstName("Mustafa");
        request.setLastName("Doğan");
        request.setEmail("TEST@GMAIL.COM");

        User user = new User();
        user.setFirstName("Mustafa");
        user.setLastName("Doğan");
        user.setEmail("test@gmail.com");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setFirstName("Mustafa");
        savedUser.setLastName("Doğan");
        savedUser.setEmail("test@gmail.com");
        savedUser.setActive(true);

        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setFirstName("Mustafa");
        response.setLastName("Doğan");
        response.setEmail("test@gmail.com");
        response.setActive(true);

        when(userRepository.existsByEmailIgnoreCase("test@gmail.com"))
                .thenReturn(false);

        when(userMapper.toEntity(request))
                .thenReturn(user);

        when(userRepository.save(user))
                .thenReturn(savedUser);

        when(userMapper.toResponse(savedUser))
                .thenReturn(response);

        UserResponse result = userService.createUser(request);

        assertEquals(1L, result.getId());
        assertEquals("Mustafa", result.getFirstName());
        assertEquals("Doğan", result.getLastName());
        assertEquals("test@gmail.com", result.getEmail());
        assertEquals(true, result.isActive());
    }

    @Test
    void testCreateUserDuplicateEmail() {

        CreateUserRequest request = new CreateUserRequest();
        request.setFirstName("Mustafa");
        request.setLastName("Doğan");
        request.setEmail("test@gmail.com");

        when(userRepository.existsByEmailIgnoreCase("test@gmail.com"))
                .thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.createUser(request)
        );

        assertEquals(
                "Email already exists",
                exception.getMessage()
        );
    }

    @Test
    void testGetUserById() {

        User user = new User();
        user.setId(1L);
        user.setFirstName("Mustafa");
        user.setLastName("Doğan");
        user.setEmail("mustafa@gmail.com");
        user.setActive(true);

        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setFirstName("Mustafa");
        response.setLastName("Doğan");
        response.setEmail("mustafa@gmail.com");
        response.setActive(true);

        when(userRepository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(user));

        when(userMapper.toResponse(user))
                .thenReturn(response);

        UserResponse result = userService.getUserById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Mustafa", result.getFirstName());
        assertEquals("Doğan", result.getLastName());
        assertEquals("mustafa@gmail.com", result.getEmail());
        assertEquals(true, result.isActive());
    }

    @Test
    void testGetUserByIdNotFound() {

        when(userRepository.findByIdAndActiveTrue(999L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserById(999L)
        );

        assertEquals("User not found", exception.getMessage());
    }
    @Test
    void testUpdateUser() {

        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("Updated");
        request.setLastName("User");
        request.setEmail("updated@gmail.com");

        User user = new User();
        user.setId(1L);
        user.setFirstName("Mustafa");
        user.setLastName("Doğan");
        user.setEmail("mustafa@gmail.com");
        user.setActive(true);

        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setFirstName("Updated");
        response.setLastName("User");
        response.setEmail("updated@gmail.com");
        response.setActive(true);

        when(userRepository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);

        when(userMapper.toResponse(user))
                .thenReturn(response);

        UserResponse result = userService.updateUser(1L, request);

        assertEquals(1L, result.getId());
        assertEquals("Updated", result.getFirstName());
        assertEquals("User", result.getLastName());
        assertEquals("updated@gmail.com", result.getEmail());
        assertEquals(true, result.isActive());
    }

    @Test
    void testUpdateUserNotFound() {

        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("Updated");
        request.setLastName("User");
        request.setEmail("updated@gmail.com");

        when(userRepository.findByIdAndActiveTrue(999L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.updateUser(999L, request)
        );

        assertEquals("User not found", exception.getMessage());
    }
    @Test
    void testDeleteUser() {

        User user = new User();
        user.setId(1L);
        user.setFirstName("Mustafa");
        user.setLastName("Doğan");
        user.setEmail("mustafa@gmail.com");
        user.setActive(true);

        when(userRepository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        assertEquals(false, user.isActive());

        verify(userRepository).save(user);
    }
    @Test
    void testDeleteUserNotFound() {

        when(userRepository.findByIdAndActiveTrue(999L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.deleteUser(999L)
        );

        assertEquals("User not found", exception.getMessage());
    }
}