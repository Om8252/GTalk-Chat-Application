package com.example.giscord.repository;

import com.example.giscord.entity.Channel;
import com.example.giscord.entity.ChannelMembership;
import com.example.giscord.entity.ChannelMembershipId;
import com.example.giscord.repository.projection.ChannelIdNameView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChannelMembershipRepository extends JpaRepository<ChannelMembership, ChannelMembershipId> {
    @Query("""
        select count(cm) > 0
        from ChannelMembership cm
        where cm.channel.channelId = :channelId
          and cm.user.userId = :userId
    """)
    boolean existsByChannelAndUser(
            @Param("channelId") Long channelId,
            @Param("userId") Long userId
    );

    @Query("""
        select cm.id.channelId from
        ChannelMembership cm
        where cm.id.userId = :userId
    """)
    public List<Long> findChannelIdByUserId(@Param("userId") Long userId);

    public List<ChannelMembership> findByUser_userId(Long userId);


    @Query("""
        select
            c.channelId as channelId,
            c.channelName as channelName
        from ChannelMembership cm
        join cm.channel c
        where cm.user.userId = :userId
    """)
    public List<ChannelIdNameView> findChannelIdAndNameFromUserId(@Param("userId") Long userId);

    public List<ChannelMembership> findByIdChannelId(Long channelId);
}
