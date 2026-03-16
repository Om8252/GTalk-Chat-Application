package com.example.giscord.ws;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import com.example.giscord.repository.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.example.giscord.redis.RedisMessage;
import com.example.giscord.security.JwtUtil;
import com.example.giscord.ws.dto.ChannelMessagePayload;
import com.example.giscord.ws.dto.WsMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final JwtUtil jwtUtil;
    private final ChannelMembershipRepository channelMembershipRepository;
    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    // Changed from new to this ?? will it ask for a Bean ?? 
    private final ObjectMapper objectMapper;
    private final AttachmentRepository attachmentRepository;
    private final RedisTemplate<String, RedisMessage> redisMessageTemplate;
    private final RedisTemplate<String, String> redisStringTemplate;

    // channelId -> sessions
    private final Map<Long, Set<WebSocketSession>> channelSessions = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(
            JwtUtil jwtUtil,
            ChannelMembershipRepository channelMembershipRepository,
            ChannelRepository channelRepository,
            MessageRepository messageRepository,
            UserRepository userRepository,
            AttachmentRepository attachmentRepository,
            RedisTemplate<String, RedisMessage> redisMessageTemplate,
            RedisTemplate<String, String> redisStringTemplate,
            ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.channelMembershipRepository = channelMembershipRepository;
        this.channelRepository = channelRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.attachmentRepository = attachmentRepository;
        this.redisMessageTemplate = redisMessageTemplate;
        this.redisStringTemplate = redisStringTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = extractToken(session.getUri());

        if (token == null || !jwtUtil.validateToken(token)) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid or missing JWT"));
            return;
        }

        Long userId = jwtUtil.getUserId(token);
        String username = jwtUtil.getUsername(token);

        if (userId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid JWT claims"));
            return;
        }

        session.getAttributes().put("userId", userId);
        session.getAttributes().put("username", username);

        System.out.println("WS authenticated userId=" + userId + ", username=" + username);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        String username = (String) session.getAttributes().get("username");


        if (userId == null) {
            sendError(session, "Unauthenticated");
            return;
        }

        WsMessage wsMessage =
                objectMapper.readValue(message.getPayload(), WsMessage.class);
        wsMessage.setUserId(userId);
        wsMessage.setUsername(username);

        // TODO: LEAVE_CHANNEL
        switch (wsMessage.getType()) {
            case "JOIN_CHANNEL" -> handleJoin(session, userId, wsMessage);
            case "CHANNEL_MESSAGE" -> handleChannelMessage(session, userId, wsMessage);
            case "FETCH_MESSAGES" -> handleFetchMessages(session, userId, wsMessage);
            default -> sendError(session, "Unknown message type");
        }
    }

    @Override
    public void afterConnectionClosed(
        WebSocketSession session,
        CloseStatus status
    ) {
        channelSessions.values().forEach(set -> set.remove(session));
        System.out.println("WS disconnected: " + session.getId());
    }

    private void handleJoin(WebSocketSession session, Long userId, WsMessage msg) throws IOException {
        Long channelId = msg.getChannelId();
        if (channelId == null) {
            sendError(session, "channelId required");
            return;
        }


        boolean isMember = channelMembershipRepository.existsByChannelAndUser(channelId, userId);

        if (!isMember) {
            sendError(session, "Not a member of channel" + channelId);
            return;
        }

        channelSessions
                .computeIfAbsent(channelId, k -> ConcurrentHashMap.newKeySet())
                .add(session);

        session.sendMessage(new TextMessage("Welcome to Channel: " + channelId));
        System.out.println("User " + userId + " joined channel " + channelId);
    }

    private void handleChannelMessage(WebSocketSession session, Long userId, WsMessage msg)
            throws Exception {

        Long channelId = msg.getChannelId();
        if (channelId == null) {
            sendError(session, "channelId required");
            return;
        }


        if (!isJoined(channelId, session)) {
            sendError(session, "Join channel before sending messages");
            return;
        }

        ChannelMessagePayload payload =
                objectMapper.convertValue(msg.getPayload(), ChannelMessagePayload.class);

        if (payload.content() == null || payload.content().isBlank()) {
            sendError(session, "content required");
            return;
        }

        if (payload.attachmentIds() != null && !payload.attachmentIds().isEmpty()) {
            long count = attachmentRepository.countByIdIn(payload.attachmentIds());
            if (count != payload.attachmentIds().size()) {
                sendError(session, "Invalid Attachment Reference");
                return;
            }
        }

        RedisMessage rm = new RedisMessage(
            channelId,
            userId,
            payload.content(),
            payload.attachmentIds(),
            Instant.now()
        );

        redisStringTemplate
                .opsForSet()
                .add("active:channels", String.valueOf(channelId));

        redisMessageTemplate
            .opsForList()
            .rightPush("channel:" + channelId, rm);




        msg.setUsername((String) session.getAttributes().get("username"));
        msg.setUserId(userId);
        String outgoing = objectMapper.writeValueAsString(msg);

        for (WebSocketSession s :
                channelSessions.getOrDefault(channelId, Set.of())) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(outgoing));
            }
        }
    }

    @Transactional(readOnly = true)
    private void handleFetchMessages(WebSocketSession session, Long userId, WsMessage msg) throws Exception {
        Long channelId = msg.getChannelId();
        if (channelId == null) {
            sendError(session, "channelId required");
            return;
        }

        boolean isMember = channelMembershipRepository.existsByChannelAndUser(channelId, userId);
        if (!isMember) {
            sendError(session, "Not a member of channel" + channelId);
            return;
        }

        // Get persisted messages from database
        var dbMessages = messageRepository.findTop50ByChannelIdWithSenderOrderByCreatedAtDesc(channelId);

        // Get recent messages from Redis
        var redisMessages = redisMessageTemplate.opsForList().range("channel:" + channelId, 0, -1);
        if (redisMessages == null) {
            redisMessages = List.of();
        }

        // Convert database messages to response format
        var dbMessageList = dbMessages.stream().map(m -> Map.of(
                "id", m.getId(),
                "channelId", m.getChannelId(),
                "userId", m.getSenderUserId(),
                "username", m.getSender().getUserName(),
                "content", m.getContent(),
                "timestamp", m.getCreatedAt().toString(),
                "attachments", m.getAttachments() != null
                        ? m.getAttachments().stream().map(a -> a.getId()).toList()
                        : List.of()
        )).toList();

        // Convert Redis messages to response format
        var redisMessageList = redisMessages.stream()
                .filter(rm -> rm.userId() != null)
                .map(rm -> {
                    var user = userRepository.findById(rm.userId()).orElse(null);
                    return Map.of(
                            "id", "redis-" + rm.createdAt().toEpochMilli(),
                            "channelId", rm.channelId(),
                            "userId", rm.userId(),
                            "username", user != null ? user.getUserName() : "Unknown",
                            "content", rm.content(),
                            "timestamp", rm.createdAt().toString(),
                            "attachments", rm.attachmentIds() != null ? rm.attachmentIds() : List.of()
                    );
                }).toList();

        // Combine and sort by timestamp descending, then take top 50
        var allMessages = Stream.concat(dbMessageList.stream(), redisMessageList.stream())
                .sorted((a, b) -> ((String) b.get("timestamp")).compareTo((String) a.get("timestamp")))
                .limit(50)
                .toList();

        var response = Map.of(
                "type", "FETCH_MESSAGES_RESPONSE",
                "channelId", channelId,
                "messages", allMessages
        );

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    private boolean isJoined(Long channelId, WebSocketSession session) {
        return channelSessions
                    .getOrDefault(channelId, Set.of())
                    .contains(session);
    }

    private String extractToken(URI uri) {
        if (uri == null || uri.getQuery() == null) return null;

        for (String param : uri.getQuery().split("&")) {
            if (param.startsWith("token=")) {
                return param.substring(6);
            }
        }
        return null;
    }

    private void sendError(WebSocketSession session, String error) {
        try {
            session.sendMessage(new TextMessage(
                    "{\"type\":\"ERROR\",\"message\":\"" + error + "\"}"
            ));
        } catch (Exception ignored) {}
    }
}
