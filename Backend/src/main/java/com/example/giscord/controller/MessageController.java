package com.example.giscord.controller;

import java.util.List;
import java.util.stream.Stream;

import com.example.giscord.entity.Attachment;
import com.example.giscord.entity.User;
import com.example.giscord.redis.RedisMessage;
import com.example.giscord.repository.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.example.giscord.dto.MessageDto;
import com.example.giscord.entity.Message;
import com.example.giscord.repository.MessageRepository;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageRepository messageRepository;
    private final RedisTemplate<String, RedisMessage> redisMessageTemplate;
    private final UserRepository userRepository;

    public MessageController(
            MessageRepository messageRepository,
            RedisTemplate<String, RedisMessage> redisMessageTemplate,
            UserRepository userRepository
    ) {
        this.messageRepository = messageRepository;
        this.redisMessageTemplate = redisMessageTemplate;
        this.userRepository = userRepository;
    }

    @GetMapping("/{channelId}")
    @Transactional(readOnly = true)
    public List<MessageDto> getMessages(@PathVariable Long channelId) {
        // TODO: Clean this up
        // TODO: MessageService -> toDto(Message) => MessageDto
        List<Message> dbMessages = messageRepository.findTop50ByChannelIdOrderByCreatedAtDesc(channelId);

        List<RedisMessage> redisMessages = redisMessageTemplate.opsForList().range("channel:" + channelId, 0, -1);
        if (redisMessages == null) {
            redisMessages = List.of();
        }

        List<MessageDto> dbMessageDtos = dbMessages.stream().map(
                m -> new MessageDto(
                        m.getId(),
                        m.getChannelId(),
                        m.getSenderUserId(),
                        m.getSender().getUserName(),
                        m.getContent(),
                        m.getCreatedAt(),
                        m.getAttachments() != null
                                ? m.getAttachments().stream().map(Attachment::getId).toList()
                                : null
                )).toList();

        List<MessageDto> redisMessageDtos = redisMessages.stream()
                .filter(rm -> rm.userId() != null)
                .map(rm -> {
                    User user = userRepository.findById(rm.userId()).orElse(null);
                            return new MessageDto(
                                    null,
                                    rm.channelId(),
                                    rm.userId(),
                                    user != null ? user.getUserName() : "Unknown",
                                    rm.content(),
                                    rm.createdAt(),
                                    rm.attachmentIds()
                            );
                }).toList();

        return Stream.concat(dbMessageDtos.stream(), redisMessageDtos.stream())
                .sorted((a, b) -> b.createdAt().compareTo(a.createdAt()))
                .toList();

    }

    @DeleteMapping("/cleanup")
    @Transactional
    public String cleanupJoinMessages() {
        List<Message> joinMessages = messageRepository.findAll().stream()
                .filter(m -> m.getContent().contains("joined the channel"))
                .toList();
        messageRepository.deleteAll(joinMessages);
        return "Deleted " + joinMessages.size() + " join messages";
    }
    
}
