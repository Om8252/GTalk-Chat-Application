package com.example.giscord.dto;

import java.time.Instant;
import java.util.List;

public record MessageDto (
    Long id,
    Long channelId,
    Long senderId,
    String senderUserName,
    String content,
    Instant createdAt,
    List<Long> attachmentIds
) {}
