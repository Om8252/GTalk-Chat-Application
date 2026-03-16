package com.example.giscord.entity;


import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class GuildMembershipId implements Serializable {
    private Long guildId;
    private Long userId;

    public GuildMembershipId() {}
    public GuildMembershipId(Long guildId, Long userId) {
        this.guildId = guildId;
        this.userId = userId;
    }

    // getters/setters
    public Long getGuildId() { return guildId; }
    public Long getUserId() { return userId; }
    public void setGuildId(Long guildId) { this.guildId = guildId; }
    public void setUserId(Long userId) { this.userId = userId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GuildMembershipId)) return false;
        GuildMembershipId that = (GuildMembershipId) o;
        return Objects.equals(guildId, that.guildId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(guildId, userId);
    }
}