package com.localmart.user_service.controller;

import com.localmart.user_service.dto.RegisterUserRequest;
import com.localmart.user_service.dto.UpdateUserRequest;
import com.localmart.user_service.dto.UserResponse;
import com.localmart.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Register and manage LocalMart users")
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user", description = "Creates a new CUSTOMER or SHOP_OWNER account")
    public UserResponse register(@RequestBody @Valid RegisterUserRequest request) {
        return userService.registerUser(request);
    }

    @GetMapping
    @Operation(summary = "Get all active users")
    public List<UserResponse> getAll() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public UserResponse getById(@PathVariable UUID id) {
        return userService.getUserById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user name, phone, and address")
    public UserResponse update(@PathVariable UUID id,
                               @RequestBody @Valid UpdateUserRequest request) {
        return userService.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Soft delete a user")
    public void delete(@PathVariable UUID id) {
        userService.deleteUser(id);
    }
}
