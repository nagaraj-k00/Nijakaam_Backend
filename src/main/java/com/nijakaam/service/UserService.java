package com.nijakaam.service;

import com.nijakaam.entity.User;

import java.util.List;

public interface UserService {
    User getUserById(String id);
    User getUserByEmail(String email);
    List<User> getAllUsers();
    void deleteUser(String id);
}
