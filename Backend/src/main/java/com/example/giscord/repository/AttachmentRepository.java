package com.example.giscord.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.giscord.entity.Attachment;

import java.util.Collection;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    long countByIdIn(Collection<Long> ids);
}
