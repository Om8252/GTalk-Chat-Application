package com.example.giscord.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "channel_memberships")
public class ChannelMembership {
    @EmbeddedId
    private ChannelMembershipId id = new ChannelMembershipId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("channelId")
    @JoinColumn(name = "channel_id")
    private Channel channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String role = "member";

    private Instant joinedAt = Instant.now();

    public ChannelMembership() {}

    public ChannelMembership(Channel channel, User user, String role) {
        this.channel = channel; this.user = user; this.role = role;
        if (channel != null) this.id.setChannelId(channel.getChannelId());
        if (user != null) this.id.setUserId(user.getUserId());
    }

    // getters/setters
    public ChannelMembershipId getId() { return id; }
    public Channel getChannel() { return channel; }
    public User getUser() { return user; }
    public String getRole() { return role; }
    public Instant getJoinedAt() { return joinedAt; }

    public void setChannel(Channel channel) { this.channel = channel; if (channel != null) this.id.setChannelId(channel.getChannelId()); }
    public void setUser(User user) { this.user = user; if (user != null) this.id.setUserId(user.getUserId()); }
    public void setRole(String role) { this.role = role; }
}

