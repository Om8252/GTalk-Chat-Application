package com.example.giscord.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.giscord.entity.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long>{
    // How the hell does this work ??
    List<Message> findTop50ByChannelIdOrderByCreatedAtDesc(Long channelId);

    List<Message> findAllByChannelIdOrderByCreatedAtDesc(Long channelId);

    @Query("""
        select m from 
        Message m 
        join fetch m.sender 
        left join fetch m.attachments 
        where m.channelId = :channelId 
        ORDER BY m.createdAt DESC
    """)
    List<Message> findTop50ByChannelIdWithSenderOrderByCreatedAtDesc(@Param("channelId") Long channelId);
}
