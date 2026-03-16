package com.example.giscord.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "guild_memberships")
public class GuildMembership {
    @EmbeddedId
    private GuildMembershipId id = new GuildMembershipId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("guildId")
    @JoinColumn(name = "guild_id")
    private Guild guild;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String role = "member";

    private Instant joinedAt = Instant.now();

    public GuildMembership() {}

    public GuildMembership(Guild guild, User user, String role) {
        this.guild = guild;
        this.user = user;
        this.role = role;
        this.id = new GuildMembershipId(guild.getGuildId(), user.getUserId());
    }

    // getters/setters
    public GuildMembershipId getId() { return id; }
    public Guild getGuild() { return guild; }
    public User getUser() { return user; }
    public String getRole() { return role; }
    public Instant getJoinedAt() { return joinedAt; }

    public void setGuild(Guild guild) {
        this.guild = guild;
        if (guild != null) this.id.setGuildId(guild.getGuildId());
    }
    public void setUser(User user) {
        this.user = user;
        if (user != null) this.id.setUserId(user.getUserId());
    }
    public void setRole(String role) { this.role = role; }
}

