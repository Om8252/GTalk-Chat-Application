package com.example.giscord.dto;

import java.util.List;

public record GuildSummaryDto(
    Long guildId,
    String guildName,
    List<ChannelSummaryDto> channels
) {}
