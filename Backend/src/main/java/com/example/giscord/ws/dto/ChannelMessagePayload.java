
package com.example.giscord.ws.dto;

import java.util.List;

public record ChannelMessagePayload(
        String content,
        List<Long> attachmentIds
) {}
