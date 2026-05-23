package com.agrorent.api.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final Cloudinary cloudinary;

    public UploadController(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Please select a file to upload.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // Upload to Cloudinary using file byte array with automatic formatting and compression optimizations
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                file.getBytes(), 
                ObjectUtils.asMap(
                    "transformation", "f_auto,q_auto,w_800,c_limit"
                )
            );

            // Extract the secure HTTPS URL
            String secureUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            Map<String, String> response = new HashMap<>();
            response.put("url", secureUrl);
            response.put("fileName", publicId);
            response.put("success", "true");

            return ResponseEntity.ok(response);

        } catch (IOException ex) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Could not upload file to Cloudinary. Please try again!");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
