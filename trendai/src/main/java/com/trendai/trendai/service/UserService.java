package com.trendai.trendai.service;

import com.trendai.trendai.dto.CreateUserRequest;
import com.trendai.trendai.dto.UpdateUserRequest;
import com.trendai.trendai.dto.UserResponse;
import com.trendai.trendai.entity.User;
import com.trendai.trendai.exception.ResourceNotFoundException;
import com.trendai.trendai.mapper.UserMapper;
import com.trendai.trendai.repository.UserRepository;
import org.springframework.stereotype.Service;
import com.trendai.trendai.exception.BusinessException;
import java.util.Locale;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(
            UserRepository userRepository,
            UserMapper userMapper
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public UserResponse createUser(CreateUserRequest request) {

        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Email already exists");
        }

        request.setEmail(email);

        User user = userMapper.toEntity(request);

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }


    public UserResponse getUserById(Long id) {

        User user = userRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        return userMapper.toResponse(user);
    }

    public UserResponse updateUser(
            Long id,
            UpdateUserRequest request
    ) {

        User user = userRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        userMapper.updateEntity(user, request);

        User updatedUser = userRepository.save(user);

        return userMapper.toResponse(updatedUser);
    }

    public void deleteUser(Long id) {

        User user = userRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        user.setActive(false);

        userRepository.save(user);
    }
}