package com.agrorent.api.controller;

import com.agrorent.api.model.User;
import com.agrorent.api.repository.UserRepository;
import com.agrorent.api.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        // Hide password hash for security
        users.forEach(u -> u.setPassword(null));
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser.getId().equals(id)) {
            return ResponseEntity.badRequest().body("You cannot delete your own admin account.");
        }
        
        return userRepository.findById(id).map(user -> {
            userRepository.delete(user);
            return ResponseEntity.ok().body("User profile successfully deleted.");
        }).orElseThrow(() -> new com.agrorent.api.exception.ResourceNotFoundException("User not found"));
    }
}
