package com.example.giscord.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "channels")
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long channelId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guild_id")
    private Guild guild;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_user_id")
    private User adminUser;

    @Column(nullable = false)
    private String channelName;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icon_attachment_id")
    private Attachment iconAttachment;

    protected Channel() {}

    public Channel(Guild guild, User adminUser, String channelName) {
        this.guild = guild;
        this.adminUser = adminUser;
        this.channelName = channelName;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getChannelId() { return channelId; }
    public Guild getGuild() { return guild; }
    public User getAdminUser() { return adminUser; }
    public String getChannelName() { return channelName; }
    public String getDescription() { return description; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public Attachment getIconAttachment() { return iconAttachment; }

    public void setChannelName(String channelName) { this.channelName = channelName; }
    public void setDescription(String description) { this.description = description; }
    public void setIconAttachment(Attachment iconAttachment) {
        this.iconAttachment = iconAttachment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Channel)) return false;
        Channel other = (Channel) o;
        return channelId != null && channelId.equals(other.channelId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
