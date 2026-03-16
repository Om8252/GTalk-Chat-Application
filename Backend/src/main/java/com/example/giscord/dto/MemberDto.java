package com.example.giscord.dto;

import java.time.Instant;

public record MemberDto(
        Long userId,
        String userName,
        String role,
        Instant joinedAt
){}