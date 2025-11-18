package org.example.dto;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class JwtResponseDTO {
    private String token;
    private String role;

    public JwtResponseDTO(String token, String role) {
        this.token = token;
        this.role = role;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
}
