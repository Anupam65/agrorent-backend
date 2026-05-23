package com.agrorent.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Long id;
    private String name;
    private String email;
    private String role;
    private String phone;
    private String address;
}
