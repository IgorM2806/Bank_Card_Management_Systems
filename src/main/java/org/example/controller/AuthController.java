package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.JwtResponseDTO;
import org.example.dto.LoginRequestDTO;
import org.example.service.CustomUserDetailsService;
import org.example.service.UserDetailsImpl;
import org.example.util.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService customUserDetailsService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtils jwtUtils,
                          CustomUserDetailsService customUserDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.customUserDetailsService = customUserDetailsService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        try {
            // Аутентификация пользователя с помощью Service Provider Interface (SPI)
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequestDTO.getNumberPhone(), loginRequestDTO.getPassword()));

            // Заполняем контекст безопасности
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Генерируем JWT-токен
            String jwtToken = jwtUtils.generateToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            return ResponseEntity.ok(new JwtResponseDTO(jwtToken, userDetails.getRole().name()));
        } catch (BadCredentialsException | DisabledException | LockedException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect credentials or account problems.");
        }
    }
}
