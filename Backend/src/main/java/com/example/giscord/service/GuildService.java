package com.example.giscord.service;

import com.example.giscord.entity.*;

import com.example.giscord.dto.GuildDto;
import com.example.giscord.dto.MemberDto;

import com.example.giscord.repository.GuildMembershipRepository;
import com.example.giscord.repository.GuildRepository;
import com.example.giscord.repository.UserRepository;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GuildService {
    private final GuildRepository guildRepo;
    private final UserRepository userRepo;
    private final GuildMembershipRepository membershipRepo;
    private final ChannelService channelService;

    public GuildService(GuildRepository guildRepo, UserRepository userRepo, GuildMembershipRepository membershipRepo, ChannelService channelService) {
        this.guildRepo = guildRepo;
        this.userRepo = userRepo;
        this.membershipRepo = membershipRepo;
        this.channelService = channelService;
    }

    @Transactional
    public Guild createGuild(String name, Long ownerUserId, String description) {
        User owner = userRepo.findById(ownerUserId).orElseThrow(() -> new IllegalArgumentException("Owner User for Guild not found"));
        Guild guild = new Guild(name);
        guild.setDescription(description);

        Guild saved = guildRepo.save(guild);
        GuildMembership gm = new GuildMembership(saved, owner, "owner");
        membershipRepo.save(gm);

        return saved;
    }

    @Transactional
    public boolean joinGuild(Long guildId, Long userId, String role) {

        if (membershipRepo.existsById(new GuildMembershipId(guildId, userId))) {
            return false;
        }

        Guild guild = guildRepo.findById(guildId).orElseThrow(() -> new IllegalArgumentException("guild not found"));
        User user = userRepo.findById(userId).orElseThrow(() -> new IllegalArgumentException("user not found"));

        GuildMembership gm = new GuildMembership(
                guild,
                user,
                role == null ? "member" : role
        );

        // TODO: Join all channels

        List<Channel> channels = channelService.findChannelsByGuildId(guildId);
        for (Channel c: channels) {
            channelService.joinChannel(c.getChannelId(), userId, role);
        }

        membershipRepo.save(gm);

        return true;
    }

    @Transactional(readOnly = true)
    public List<Guild> findGuildsByUserId(Long userId) {
        List<GuildMembership> debugList = membershipRepo.findByUser_userId(userId);
        return membershipRepo.findByUser_userId(userId).stream()
                .map(GuildMembership::getGuild)
                .toList();
    }

    public Optional<Guild> findById(Long id) {
        return guildRepo.findById(id);
    }

    @Transactional(readOnly = true)
    public GuildDto toDto(Guild guild, int memberLimit) {
        List<MemberDto> members = membershipRepo.findByIdGuildId(guild.getGuildId()).stream()
                .limit(memberLimit > 0 ? memberLimit : Long.MAX_VALUE)
                .map(gm -> new MemberDto(
                        gm.getUser().getUserId(),
                        gm.getUser().getUserName(),
                        gm.getRole(),
                        gm.getJoinedAt()
                ))
                .toList();

        return new GuildDto(
                guild.getGuildId(),
                guild.getGuildName(),
                guild.getDescription(),
                guild.getCreatedAt(),
                guild.getUpdatedAt(),
                guild.getIconAttachment() != null
                        ? guild.getIconAttachment().getId()
                        : null,
                members
        );
    }

    public boolean setIcon(Long guildId, Attachment attachment) throws Exception {
        Guild guild = guildRepo.findById(guildId).orElseThrow(() -> new BadRequestException("GuildId is invalid ..."));
        guild.setIconAttachment(attachment);
        guildRepo.save(guild);
        return true;
    }
}

