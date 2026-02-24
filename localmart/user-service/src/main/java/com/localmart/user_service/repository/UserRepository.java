package com.localmart.user_service.repository;

import com.localmart.user_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    // Used by getAllUsers — returns only non-deleted users
    List<User> findByActiveTrue();

    // Used by getById, updateUser, deleteUser — only finds non-deleted users
    Optional<User> findByIdAndActiveTrue(UUID id);

    // Used for duplicate check on email before registering
    boolean existsByEmail(String email);

    // Used for duplicate check on phone before registering
    boolean existsByPhone(String phone);
}
