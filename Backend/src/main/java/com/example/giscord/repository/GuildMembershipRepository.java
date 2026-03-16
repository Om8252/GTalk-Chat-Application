package com.example.giscord.repository;

import com.example.giscord.entity.Guild;
import com.example.giscord.entity.GuildMembership;
import com.example.giscord.entity.GuildMembershipId;
import com.example.giscord.repository.projection.GuildIdNameView;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuildMembershipRepository extends JpaRepository<GuildMembership, GuildMembershipId> {
    @Query("""
       select gm.id.guildId from
       GuildMembership gm
       where gm.id.userId = :userId
    """)
    public List<Long> findGuildIdByUserId(@Param("userId") Long userId);


    @Query("""
        select 
            g.guildId as guildId,
            g.guildName as guildName
        from GuildMembership gm
        join gm.guild g
        where gm.user.userId = :userId
    """)
    public List<GuildIdNameView> findGuildIdAndNameFromUserId(@Param("userId") Long userId);


    // TODO: Modify this to findByGuild_guildId ?? check if that's more idiomatic (also, consider performance)
    List<GuildMembership> findByIdGuildId(Long guildId);

    @Query("""
        select gm
        from GuildMembership gm
        join fetch gm.guild g
        where gm.user.userId = :userId
    """)
    List<GuildMembership> findByUser_userId(Long userId);

    boolean existsById(GuildMembershipId id);
}
