package com.nijakaam.service;

import com.nijakaam.dto.request.LoginRequest;
import com.nijakaam.dto.request.RegisterRequest;
import com.nijakaam.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
