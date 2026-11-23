package com.nijakaam.controller;

import com.nijakaam.dto.response.ApiResponse;
import com.nijakaam.dto.response.UserResponse;
import com.nijakaam.entity.User;
import com.nijakaam.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable String id) {

        User user = userService.getUserById(id);

        UserResponse response = mapToUserResponse(user);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Get all users
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {

        List<User> users = userService.getAllUsers();

        List<UserResponse> responses = users.stream()
                .map(this::mapToUserResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // Delete user
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable String id) {

        userService.deleteUser(id);

        return ResponseEntity.ok(
                ApiResponse.success("User deleted successfully", null));
    }

    // Convert User entity to safe UserResponse DTO
    private UserResponse mapToUserResponse(User user) {

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}