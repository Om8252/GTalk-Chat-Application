package com.example.giscord.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.giscord.entity.Channel;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {

    // Why are there left joins here ??
    // What exactly do they mean ??
    // Are they optimized ?? Overengineered ??
//    @Query("select c from Channel c " +
//            "left join fetch c.members cm " +
//            "left join fetch cm.user u " +
//            "where c.channelId = :id")
//    Optional<Channel> findByIdWithMembersAndUsers(@Param("id") Long id);
//
//    @Query("select c from Channel c " +
//            "left join fetch c.messages m " +
//            "left join fetch m.sender s " +
//            "where c.channelId = :id")
//    Optional<Channel> findByIdWithMessagesAndSenders(@Param("id") Long id);

    public List<Channel> findByGuild_guildId(Long guildId);
}

