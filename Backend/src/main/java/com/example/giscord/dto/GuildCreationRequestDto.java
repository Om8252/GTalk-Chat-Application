package com.example.giscord.dto;

public record GuildCreationRequestDto(
    String guildName,
    Long ownerId,
    String description
) {}
