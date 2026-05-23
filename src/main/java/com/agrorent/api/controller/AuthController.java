package com.agrorent.api.controller;

import com.agrorent.api.dto.*;
import com.agrorent.api.exception.BadRequestException;
import com.agrorent.api.model.User;
import com.agrorent.api.repository.UserRepository;
import com.agrorent.api.security.JwtTokenProvider;
import com.agrorent.api.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = principal.getUser();
        User freshUser = userRepository.findById(user.getId()).orElse(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", freshUser.getId());
        response.put("name", freshUser.getName());
        response.put("email", freshUser.getEmail());
        response.put("role", freshUser.getRole().name());
        response.put("phone", freshUser.getPhone());
        response.put("address", freshUser.getAddress());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getEmail(),
                        authRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        return ResponseEntity.ok(new AuthResponse(
                jwt,
                principal.getId(),
                principal.getName(),
                principal.getUsername(),
                principal.getUser().getRole().name(),
                principal.getUser().getPhone(),
                principal.getUser().getAddress()
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("Email address is already in use!");
        }

        User user = User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(registerRequest.getRole())
                .phone(registerRequest.getPhone())
                .address(registerRequest.getAddress())
                .build();

        userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User registered successfully");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
