package com.example.giscord.dto;

import java.time.Instant;
import java.util.List;

public record GuildDto(
        Long guildId,
        String guildName,
        String description,
        Instant createdAt,
        Instant updatedAt,
        Long iconAttachmentId,
        List<MemberDto> members
){}
