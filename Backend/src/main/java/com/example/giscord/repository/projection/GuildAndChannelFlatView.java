package com.example.giscord.repository.projection;

public interface GuildAndChannelFlatView {
    Long getGuildId();
    String getGuildName();
    Long getChannelId();
    String getChannelName();
}
