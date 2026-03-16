package com.example.giscord.dto;

import java.time.Instant;
import java.util.List;

public record ChannelDto (
        Long channelId,
        Long guildId,
        Long adminUserId,
        String channelName,
        String description,
        Instant createdAt,
        Instant updatedAt,
        Long iconAttachmentId,
        List<MemberDto> members
) {}
