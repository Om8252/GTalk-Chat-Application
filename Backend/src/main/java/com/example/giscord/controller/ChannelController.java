package com.example.giscord.controller;

import java.util.List;
import java.util.Map;

import com.example.giscord.dto.ChannelCreationRequestDto;
import org.apache.coyote.BadRequestException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.giscord.dto.ChannelDto;
import com.example.giscord.entity.Attachment;
import com.example.giscord.entity.Channel;
import com.example.giscord.repository.AttachmentRepository;
import com.example.giscord.repository.MessageRepository;
import com.example.giscord.security.CustomUserDetails;
import com.example.giscord.service.ChannelService;


@RestController
@RequestMapping("/api/channels")
public class ChannelController {

    private final ChannelService channelService;
    private final MessageRepository messageRepository;
    private final AttachmentRepository attachmentRepository;

    public ChannelController(ChannelService channelService, MessageRepository messageRepository, AttachmentRepository attachmentRepository) {
        this.channelService = channelService;
        this.messageRepository = messageRepository;
        this.attachmentRepository = attachmentRepository;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@RequestBody ChannelCreationRequestDto body, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long guildId = body.guildId();
        Long adminUserId = userDetails.getUserId();
        String channelName = body.name();
        try {
            Channel c = channelService.createChannel(guildId, adminUserId, channelName);
            ChannelDto dto = channelService.toDto(c, 10);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
   }

    @PostMapping(path = "/{channelId}/join", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> join(@PathVariable Long channelId, @RequestBody Map<String, String> body) {
        String role = null;
        if (body.containsKey("role")) {
            role = body.get("role");
        }

        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof CustomUserDetails cud)) {
            return ResponseEntity.status(401).body(Map.of("error", "not authenticated"));
        }
        Long userId = cud.getUserId();
        System.out.println("*****************************");
        System.out.println("*****************************");
        System.out.println("*****************************");
        System.out.println("*****************************");
        System.out.println(userId);
        System.out.println("*****************************");
        System.out.println("*****************************");
        System.out.println("*****************************");
        System.out.println("*****************************");
        try {
            Channel c = channelService.joinChannel(channelId, userId, role);
            ChannelDto dto = channelService.toDto(c, 50);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/guild/{guildId}")
    public ResponseEntity<List<ChannelDto>> getUserChannels(@PathVariable Long guildId) {
        List<Channel> channels = channelService.findChannelsByGuildId(guildId);
        return ResponseEntity.ok(channels.stream()
                .map(c -> channelService.toDto(c, 50))
                .toList());
    }


    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getChannelById(@PathVariable Long id) throws Exception {
        ChannelDto channelDto = channelService.getChannelById(id);
        return ResponseEntity.ok(channelDto);
    }

    @PostMapping("/{id}/icon")
    public ResponseEntity<?> setIcon(@PathVariable Long id, @RequestBody Map<String, Long> req) throws Exception {
        Long attachmentId = req.get("attachmentId");

        Attachment attachment = attachmentRepository.findById(attachmentId).orElseThrow(() -> new BadRequestException("Invalid attachment Id ..."));

        channelService.setIcon(id, attachment);
        return ResponseEntity.ok(Map.of("updated_attachment_id", attachmentId));
    }
}

