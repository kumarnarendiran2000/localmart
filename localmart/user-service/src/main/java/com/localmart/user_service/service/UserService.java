package com.localmart.user_service.service;

import com.localmart.user_service.dto.RegisterUserRequest;
import com.localmart.user_service.dto.UpdateUserRequest;
import com.localmart.user_service.dto.UserResponse;
import com.localmart.user_service.exception.DuplicateResourceException;
import com.localmart.user_service.exception.ResourceNotFoundException;
import com.localmart.user_service.model.User;
import com.localmart.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    // ─── WRITE OPERATIONS ───────────────────────────────────────────────────
    // @Transactional on write methods: Spring opens a DB transaction before the method
    // runs and commits it when the method returns. If an exception is thrown, it rolls back.
    // Without this, each repository call is its own transaction — risky for multi-step writes.

    @Transactional
    public UserResponse registerUser(RegisterUserRequest request) {
        log.info("Registering user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "A user with email '" + request.getEmail() + "' already exists.");
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException(
                    "A user with phone '" + request.getPhone() + "' already exists.");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .role(request.getRole())
                .build();

        User saved = userRepository.save(user);
        log.info("User registered with id: {}", saved.getId());
        return toUserResponse(saved);
    }

    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = findActiveUser(id);

        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());

        User saved = userRepository.save(user);
        log.info("User updated: {}", id);
        return toUserResponse(saved);
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = findActiveUser(id);
        user.setActive(false);
        userRepository.save(user);
        log.info("User soft-deleted: {}", id);
    }

    // ─── READ OPERATIONS ────────────────────────────────────────────────────
    // @Transactional(readOnly = true): tells Hibernate this transaction will not
    // write anything. Hibernate skips dirty checking (comparing every field before
    // flush) → faster. Also allows the DB to use a read replica if one is configured.

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        return toUserResponse(findActiveUser(id));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findByActiveTrue()
                .stream()
                .map(user -> toUserResponse(user))
                .toList();
    }

    // ─── PRIVATE HELPERS ────────────────────────────────────────────────────

    private User findActiveUser(UUID id) {
        return userRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .role(user.getRole())
                .build();
    }
}
