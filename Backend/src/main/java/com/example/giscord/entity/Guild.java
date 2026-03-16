package com.example.giscord.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "guilds")
public class Guild {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long guildId;

    @Column(nullable = false)
    private String guildName;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icon_attachment_id")
    private Attachment iconAttachment;

    protected Guild() {}

    public Guild(String guildName) {
        this.guildName = guildName;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getGuildId() { return guildId; }
    public String getGuildName() { return guildName; }
    public String getDescription() { return description; }
    public Attachment getIconAttachment() { return iconAttachment; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setGuildName(String guildName) { this.guildName = guildName; }
    public void setDescription(String description) { this.description = description; }
    public void setIconAttachment(Attachment iconAttachment) {
        this.iconAttachment = iconAttachment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Guild)) return false;
        Guild other = (Guild) o;
        return guildId != null && guildId.equals(other.guildId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

