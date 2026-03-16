package com.example.giscord.dto;

import java.time.Instant;

public record UserResponseDto (
    Long userId,
    String userName,
    String description,
    Instant createdAt,
    Instant updatedAt,
    Long profileAttachmentId
) {}
