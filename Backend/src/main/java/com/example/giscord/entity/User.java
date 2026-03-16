package com.example.giscord.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String userName;

    @Column(nullable = false)
    private String passwordHash;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_attachment_id")
    private Attachment profileAttachment;

    protected User() {}

    public User(String userName, String passwordHash) {
        this.userName = userName;
        this.passwordHash = passwordHash;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getPasswordHash() { return passwordHash; }
    public String getDescription() { return description; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Attachment getProfileAttachment() { return profileAttachment; }

    public void setUserName(String userName) { this.userName = userName; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setDescription(String description) { this.description = description; }
    public void setProfileAttachment(Attachment profileAttachment) {
        this.profileAttachment = profileAttachment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User other = (User) o;
        return userId != null && userId.equals(other.userId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
