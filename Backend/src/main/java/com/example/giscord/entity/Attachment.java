package com.example.giscord.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "attachments")
@Getter
@Setter
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bucket;
    private String objectKey;
    private String contentType;
    private long size;

    @ManyToMany(mappedBy = "attachments", fetch = FetchType.LAZY)
    private final Set<Message> messages = new HashSet<>();

    public void addMessage(Message message) {
        messages.add(message);
        message.getAttachments().add(this);
    }

    public void removeMessage(Message message) {
        messages.remove(message);
        message.getAttachments().remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Attachment)) return false;
        Attachment other = (Attachment) o;
        return id != null && id.equals(other.id);
    }

    // TODO: Check if this is correct ??
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
