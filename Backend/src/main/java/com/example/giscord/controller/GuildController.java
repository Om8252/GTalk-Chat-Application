
package com.example.giscord.controller;

import com.example.giscord.dto.GuildCreationRequestDto;
import com.example.giscord.dto.GuildDto;
import com.example.giscord.entity.Attachment;
import com.example.giscord.entity.Guild;
import com.example.giscord.repository.AttachmentRepository;
import com.example.giscord.security.CustomUserDetails;
import com.example.giscord.service.GuildService;
import com.example.giscord.service.UserService;
import org.apache.coyote.BadRequestException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/guilds")
public class GuildController {
    private final GuildService guildService;
    private final UserService userService;
    private final AttachmentRepository attachmentRepository;

    public GuildController(GuildService guildService, AttachmentRepository attachmentRepository, UserService userService) {
        this.guildService = guildService;
        this.attachmentRepository = attachmentRepository;
        this.userService = userService;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GuildDto> create(@RequestBody GuildCreationRequestDto req, Authentication authentication) {
        // JUGAAD-ALERT:
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Guild guild = guildService.createGuild(req.guildName(), userDetails.getUserId(), req.description());
        GuildDto dto = guildService.toDto(guild, 10);
        return ResponseEntity.ok(dto);
    }

    @PostMapping(path = "/{guildId}/join", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> join(@PathVariable Long guildId, @RequestBody Map<String, String> body) {
        Long userId = Long.valueOf(body.get("userId"));
        String role = body.get("role"); // optional
        try {
            // TODO: Write Decent Code dude ...
            Guild g = guildService.joinGuild(guildId, userId, role) == true ? guildService.findById(guildId).get(): null;
            if (g == null) {
                return ResponseEntity.badRequest().body("Already Joined ...");
            }
            GuildDto gto = guildService.toDto(g, 10);
            return ResponseEntity.ok(gto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GuildDto>> getUserGuilds(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        List<Guild> guilds = guildService.findGuildsByUserId(userDetails.getUserId());
        // TODO: Check and decide if defaultGuild is a necessity ???
        if (guilds.isEmpty()) {
            Guild defaultGuild = guildService.createGuild("Welcome Guild", userDetails.getUserId(), "This is default guild and not really sure if it's necessary currently or not");
            guildService.joinGuild(defaultGuild.getGuildId(), userDetails.getUserId(), "owner");
            guilds = List.of(defaultGuild);
        }

        List<GuildDto> dtos = guilds.stream()
                .map(g -> guildService.toDto(g, 50))
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GuildDto> get(@PathVariable Long id) {
        return guildService.findById(id)
                .map(g -> ResponseEntity.ok(guildService.toDto(g, 10)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/icon")
    public ResponseEntity<?> setIcon(@PathVariable Long id, @RequestBody Map<String, Long> req) throws Exception {
        Long attachmentId = req.get("attachmentId");
        Attachment attachment = attachmentRepository.findById(attachmentId).orElseThrow(() -> new BadRequestException("AttachmentId not found"));

        guildService.setIcon(id, attachment);

        return ResponseEntity.ok(Map.of("updated_icon_id", attachmentId));

    }
}

