package com.example.giscord.service;

import com.example.giscord.dto.ChannelSummaryDto;
import com.example.giscord.dto.GuildDto;
import com.example.giscord.dto.GuildSummaryDto;
import com.example.giscord.dto.UserResponseDto;
import com.example.giscord.entity.Attachment;
import com.example.giscord.entity.User;
import com.example.giscord.repository.ChannelMembershipRepository;
import com.example.giscord.repository.GuildMembershipRepository;
import com.example.giscord.repository.GuildRepository;
import com.example.giscord.repository.UserRepository;
import com.example.giscord.repository.projection.ChannelIdNameView;
import com.example.giscord.repository.projection.GuildAndChannelFlatView;
import com.example.giscord.repository.projection.GuildIdNameView;
import org.apache.coyote.BadRequestException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final ChannelMembershipRepository channelMembershipRepository;
    private final GuildMembershipRepository guildMembershipRepository;
    private final GuildRepository guildRepository;
    private final BCryptPasswordEncoder encoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder encoder,
                       ChannelMembershipRepository channelMembershipRepository, GuildMembershipRepository guildMembershipRepository,
                       GuildRepository guildRepository) {
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.channelMembershipRepository = channelMembershipRepository;
        this.guildMembershipRepository = guildMembershipRepository;
        this.guildRepository = guildRepository;
    }

    public User registerUser(String userName, String plainPassword, String description) {
        String hashed = encoder.encode(plainPassword);
        User user = new User(userName, hashed);
        user.setDescription(description); // TODO: why not modify the ctor ??
        return userRepository.save(user);
    }

    public boolean verifyPassword(String userName, String plainPassword) {
        return userRepository.findByUserName(userName)
                .map(user -> encoder.matches(plainPassword, user.getPasswordHash()))
                .orElse(false);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUserName(String username) {
        return userRepository.findByUserName(username);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public UserResponseDto toDto(User user) {
        return new UserResponseDto(
            user.getUserId(),
            user.getUserName(),
            user.getDescription(),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            user.getProfileAttachment() != null
                ? user.getProfileAttachment().getId()
                : null
        );
    }

    @Transactional(readOnly = true)
    public List<GuildSummaryDto> getAllGuildsAndChannels(Long userId) {
        List<GuildAndChannelFlatView> guildAndChannels = guildRepository.findGuildsWithChannels(userId);

        Map<Long, GuildSummaryDto> gid_gto = new LinkedHashMap<>();

        for (var row: guildAndChannels) {
            gid_gto.computeIfAbsent(row.getGuildId(), id ->
                        new GuildSummaryDto(id, row.getGuildName(), new ArrayList<>())
                    );

            if (row.getChannelId() != null) {
                gid_gto.get(row.getGuildId())
                        .channels()
                        .add(new ChannelSummaryDto(row.getChannelId(), row.getChannelName()));
            }
        }

        return List.copyOf(gid_gto.values());
    }

    public List<ChannelIdNameView> getAllChannelIdsAndNamesByUserId(Long userId) {
        return channelMembershipRepository.findChannelIdAndNameFromUserId(userId);
    }

    public List<GuildIdNameView> getAllGuildIdsAndNamesByUserId(Long userId) {
        return guildMembershipRepository.findGuildIdAndNameFromUserId(userId);
    }

    public boolean setProfilePicture(Long userId, Attachment attachment) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Invalid userId it seems ..."));
        user.setProfileAttachment(attachment);
        userRepository.save(user);
        return true;
    }

    public boolean updateDescription(Long userId, String description) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Invalid userId it seems ..."));
        user.setDescription(description);
        userRepository.save(user);
        return true;
    }
}
