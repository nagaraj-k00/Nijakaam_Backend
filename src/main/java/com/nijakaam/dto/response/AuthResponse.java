package com.nijakaam.dto.response;

import com.nijakaam.enums.Role;

public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private String userId;
    private String email;
    private Role role;

    public AuthResponse() {
    }

    public AuthResponse(String token, String userId, String email, Role role) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
