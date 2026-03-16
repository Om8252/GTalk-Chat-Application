package com.example.giscord.service;

import com.example.giscord.dto.ChannelDto;
import com.example.giscord.dto.MemberDto;
import com.example.giscord.dto.MessageDto;
import com.example.giscord.entity.*;
import com.example.giscord.repository.*;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
public class ChannelService {

    private final ChannelRepository channelRepo;
    private final GuildRepository guildRepo;
    private final UserRepository userRepo;
    private final ChannelMembershipRepository membershipRepo;
    private final MessageRepository messageRepo;

    public ChannelService(ChannelRepository channelRepo,
                          GuildRepository guildRepo,
                          UserRepository userRepo,
                          ChannelMembershipRepository membershipRepo,
                          MessageRepository messageRepo) {
        this.channelRepo = channelRepo;
        this.guildRepo = guildRepo;
        this.userRepo = userRepo;
        this.membershipRepo = membershipRepo;
        this.messageRepo = messageRepo;
    }

    @Transactional
    public Channel createChannel(Long guildId, Long adminUserId, String channelName) {
        Guild guild = guildRepo.findById(guildId).orElseThrow(() -> new IllegalArgumentException("guild not found"));
        User admin = userRepo.findById(adminUserId).orElseThrow(() -> new IllegalArgumentException("user not found"));
        Channel c = new Channel(guild, admin, channelName);
        Channel saved = channelRepo.save(c);

        // create admin membership
        ChannelMembership cm = new ChannelMembership();
        cm.setChannel(saved);
        cm.setUser(admin);
        cm.setRole("owner");
        cm.getId().setChannelId(saved.getChannelId());
        cm.getId().setUserId(admin.getUserId());
        membershipRepo.save(cm);

        return saved;
    }

    @Transactional
    public Channel joinChannel(Long channelId, Long userId, String role) {
        Channel channel = channelRepo.findById(channelId).orElseThrow(() -> new IllegalArgumentException("channel not found"));
        User user = userRepo.findById(userId).orElseThrow(() -> new IllegalArgumentException("user not found"));

        ChannelMembershipId id = new ChannelMembershipId(channelId, userId);
        if (membershipRepo.existsById(id)) return channel;

        ChannelMembership cm = new ChannelMembership();
        cm.setChannel(channel);
        cm.setUser(user);
        cm.setRole(role == null ? "member" : role);
        cm.getId().setChannelId(channelId);
        cm.getId().setUserId(userId);
        membershipRepo.save(cm);

        return channel;
    }

    @Transactional(readOnly = true)
    public ChannelDto toDto(Channel c, int memberLimit) {
        List<MemberDto> members = membershipRepo.findByIdChannelId(c.getChannelId()).stream()
                .limit(memberLimit > 0 ? memberLimit : Long.MAX_VALUE)
                .map(cm -> new MemberDto(
                        cm.getUser().getUserId(),
                        cm.getUser().getUserName(),
                        cm.getRole(),
                        cm.getJoinedAt()
                ))
                .toList();

        return new ChannelDto(
                c.getChannelId(),
                c.getGuild().getGuildId(),
                c.getAdminUser().getUserId(),
                c.getChannelName(),
                c.getDescription(),
                c.getCreatedAt(),
                c.getUpdatedAt(),
                c.getIconAttachment() != null
                        ? c.getIconAttachment().getId()
                        : null,
                members
        );
    }

    @Transactional(readOnly = true)
    public List<MessageDto> listMessages(Long channelId, int limit) {
        var page = PageRequest.of(0, Math.max(1, limit));
        List<Message> msgs = messageRepo.findTop50ByChannelIdOrderByCreatedAtDesc(channelId);
        return msgs.stream()
                .map(m -> new MessageDto(
                        m.getId(),
                        m.getChannelId(),
                        m.getSender().getUserId(),
                        m.getSender().getUserName(),
                        m.getContent(),
                        m.getCreatedAt(),
                        m.getAttachments().stream()
                                .map(Attachment::getId)
                                .toList()
                )).toList();
    }

    public List<Channel> findChannelsByUserId(Long userId) {
        return membershipRepo.findByUser_userId(userId).stream()
                .map(cm -> cm.getChannel())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Channel> findChannelsByGuildId(Long guildId) {
        return channelRepo.findByGuild_guildId(guildId);
    }

    public ChannelDto getChannelById(Long id) throws Exception {
        Channel channel = channelRepo.findById(id).orElseThrow(() -> new BadRequestException("Invalid channelId ..."));
        return toDto(channel, 50);
    }

    public boolean setIcon(Long channelId, Attachment attachment) throws Exception {
        Channel channel = channelRepo.findById(channelId).orElseThrow(() -> new BadRequestException("ChannelId in invalid ..."));

        channel.setIconAttachment(attachment);
        channelRepo.save(channel);

        return true;
    }
}

