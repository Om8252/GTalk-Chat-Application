package com.example.giscord.dto;

public record ChannelCreationRequestDto(
        Long guildId,
        String name
) { }
