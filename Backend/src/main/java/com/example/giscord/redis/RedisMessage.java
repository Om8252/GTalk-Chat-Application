package com.example.giscord.redis;

import java.time.Instant;
import java.util.List;

public record RedisMessage (
    Long channelId,
    Long userId,
    String content,
    List<Long> attachmentIds,
    Instant createdAt
){}
