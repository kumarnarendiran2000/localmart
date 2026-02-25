package com.localmart.user_service.controller;

import com.localmart.user_service.dto.RegisterUserRequest;
import com.localmart.user_service.dto.UpdateUserRequest;
import com.localmart.user_service.dto.UserResponse;
import com.localmart.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Users", description = "Register and manage LocalMart users")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @Operation(summary = "Register a new user", description = "Creates a new CUSTOMER or SHOP_OWNER account")
    @ApiResponse(responseCode = "201", description = "User registered successfully")
    @ApiResponse(responseCode = "400", description = "Validation failed — check errors field in response")
    @ApiResponse(responseCode = "409", description = "Email or phone already registered")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterUserRequest request) {
        log.debug("POST /api/users - email: {}", request.getEmail());
        return userService.registerUser(request);
    }

    @Operation(summary = "Get all active users")
    @ApiResponse(responseCode = "200", description = "List returned (empty array if none exist)")
    @GetMapping
    public List<UserResponse> getAll() {
        log.debug("GET /api/users");
        return userService.getAllUsers();
    }

    @Operation(summary = "Get user by ID")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping("/{id}")
    public UserResponse getById(
            @Parameter(description = "User UUID") @PathVariable UUID id) {
        log.debug("GET /api/users/{}", id);
        return userService.getUserById(id);
    }

    @Operation(summary = "Update user name, phone, and address")
    @ApiResponse(responseCode = "200", description = "User updated")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PutMapping("/{id}")
    public UserResponse update(
            @Parameter(description = "User UUID") @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        log.debug("PUT /api/users/{}", id);
        return userService.updateUser(id, request);
    }

    @Operation(summary = "Soft-delete a user")
    @ApiResponse(responseCode = "204", description = "User deleted (active set to false)")
    @ApiResponse(responseCode = "404", description = "User not found")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "User UUID") @PathVariable UUID id) {
        log.debug("DELETE /api/users/{}", id);
        userService.deleteUser(id);
    }
}
