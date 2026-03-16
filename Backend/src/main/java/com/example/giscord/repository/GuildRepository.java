package com.example.giscord.repository;

import com.example.giscord.entity.Guild;
import com.example.giscord.repository.projection.GuildAndChannelFlatView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuildRepository extends JpaRepository<Guild, Long> {
    @Query("""
            select 
                g.guildId as guildId, 
                g.guildName as guildName, 
                c.channelId as channelId, 
                c.channelName as channelName
            from Guild g 
            left join Channel c on c.guild = g
            where g.guildId in (
                select gm.id.guildId
                from GuildMembership gm
                where gm.id.userId = :userId
            )
    """)
    List<GuildAndChannelFlatView> findGuildsWithChannels(@Param("userId") Long userId);
}

