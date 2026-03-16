package com.example.giscord.service;

import java.util.List;
import java.util.Set;

import com.example.giscord.entity.User;
import com.example.giscord.repository.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.giscord.entity.Attachment;
import com.example.giscord.entity.Message;
import com.example.giscord.redis.RedisMessage;
import com.example.giscord.repository.AttachmentRepository;
import com.example.giscord.repository.MessageRepository;

@Service
public class MessageFlushService {

    private final RedisTemplate<String, RedisMessage> redisMessageTemplate;
    private final RedisTemplate<String, String> redisStringTemplate;
    private final MessageRepository messageRepository;
    private final AttachmentRepository attachmentRepository;
    private final UserRepository userRepository;

    public MessageFlushService(
            RedisTemplate<String, RedisMessage> redisMessageTemplate,
            RedisTemplate<String, String> redisStringTemplate,
            MessageRepository messageRepository,
            AttachmentRepository attachmentRepository,
            UserRepository userRepository
    ) {
        this.redisMessageTemplate = redisMessageTemplate;
        this.redisStringTemplate = redisStringTemplate;
        this.messageRepository = messageRepository;
        this.attachmentRepository = attachmentRepository;
        this.userRepository = userRepository;
    }


    @Scheduled(fixedDelay = 5000)
    public void flush() {
        Set<String> channelIds = redisStringTemplate.opsForSet().members("active:channels");

        if (channelIds == null || channelIds.isEmpty()) {
            return;
        }

        for (String channelId: channelIds) {
            String key = "channel:" + channelId;

            List<RedisMessage> batch = redisMessageTemplate.opsForList().range(key, 0, -1);

            if (batch == null || batch.isEmpty()) {
                redisStringTemplate.opsForSet().remove("active:channels", channelId);
                continue;
            }
            
            redisMessageTemplate.delete(key);
            redisStringTemplate.opsForSet().remove("active:channels", channelId);

            for (RedisMessage rm: batch) {

                if (rm.userId() == null) {
                    System.err.println("Skipping message with null userId: " + rm);
                    continue;
                }

                // TODO: Root Cause Analysis (Why even are userId's comming as nulls ??)
                User sender = userRepository.findById(rm.userId()).orElse(null);
                if (sender == null) {
                    System.err.println("Skipping message with invalid userId: " + rm.userId());
                    continue;
                }

                Message m = new Message();
                m.setChannelId(rm.channelId());
                m.setSender(sender);
                m.setContent(rm.content());
                m.setCreatedAt(rm.createdAt());
                
                if (rm.attachmentIds() != null && !rm.attachmentIds().isEmpty()) {
                    List<Attachment> attachments = attachmentRepository.findAllById(rm.attachmentIds());
                    attachments.forEach(m::addAttachment);
                }
                messageRepository.save(m);
            }
        }
    }
}
