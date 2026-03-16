package com.example.giscord.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ChannelMembershipId implements Serializable {
    private Long channelId;
    private Long userId;

    public ChannelMembershipId() {}
    public ChannelMembershipId(Long channelId, Long userId) {
        this.channelId = channelId; this.userId = userId;
    }

    public Long getChannelId() { return channelId; }
    public Long getUserId() { return userId; }
    public void setChannelId(Long channelId) { this.channelId = channelId; }
    public void setUserId(Long userId) { this.userId = userId; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChannelMembershipId)) return false;
        ChannelMembershipId that = (ChannelMembershipId) o;
        return Objects.equals(channelId, that.channelId) && Objects.equals(userId, that.userId);
    }

    @Override public int hashCode() { return Objects.hash(channelId, userId); }
}

