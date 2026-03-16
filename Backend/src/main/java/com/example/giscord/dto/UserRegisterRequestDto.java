package com.example.giscord.dto;

public record UserRegisterRequestDto (
    String username,
    String password,
    String description
) {}
