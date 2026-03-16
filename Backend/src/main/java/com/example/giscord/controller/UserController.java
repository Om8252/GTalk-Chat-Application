package com.example.giscord.controller;

import java.util.Map;

import org.apache.coyote.BadRequestException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.giscord.dto.UserLoginRequestDto;
import com.example.giscord.dto.UserRegisterRequestDto;
import com.example.giscord.dto.UserResponseDto;
import com.example.giscord.entity.Attachment;
import com.example.giscord.entity.User;
import com.example.giscord.repository.AttachmentRepository;
import com.example.giscord.security.JwtUtil;
import com.example.giscord.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AttachmentRepository attachmentRepository; // TODO: Don't be lazy write a service instead

    public UserController(UserService userService, JwtUtil jwtUtil, AttachmentRepository attachmentRepository) {
        this.userService = userService; 
        this.jwtUtil = jwtUtil;
        this.attachmentRepository = attachmentRepository;
    }

    @PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> register(@RequestBody UserRegisterRequestDto req) {
        if (req.username() == null || req.username().isBlank()
                || req.password() == null || req.password().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "username and password required"));
        }

        try {
            User created = userService.registerUser(req.username(), req.password(), req.description());
            UserResponseDto dto = userService.toDto(created);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(dto);
        } catch (Exception e) {
            // handle duplicate username or DB constraint violations
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@RequestBody UserLoginRequestDto req) {
        if (req.username() == null || req.password() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "username and password required"));
        }

        // authenticate using AuthenticationManager or manual check + token creation
        boolean ok = userService.verifyPassword(req.username(), req.password());
        if (!ok) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid credentials"));
        }

        // load user and create token
        var userOpt = userService.findByUserName(req.username()); // add this helper in service
        if (userOpt.isEmpty()) return ResponseEntity.status(500).body(Map.of("error", "unexpected"));

        var user = userOpt.get();
        String token = jwtUtil.generateToken(user.getUserId(), user.getUserName());


        UserResponseDto dto = userService.toDto(user);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(Map.of("token", token, "user", dto));
    }


    // Optional helper endpoint (dev): get basic user info
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        return userService.findById(id)
                .map(u -> ResponseEntity.ok().body(userService.toDto(u)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/loadui")
    public ResponseEntity<?> getGuildAndChannels(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getAllGuildsAndChannels(id));
    }


    @GetMapping("/{id}/guilds")
    public ResponseEntity<?> getGuilds(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getAllGuildIdsAndNamesByUserId(id));
    }

    @GetMapping("/{id}/channels")
    public ResponseEntity<?> getChannels(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getAllChannelIdsAndNamesByUserId(id));
    }

    // TODO: Move away from try-catch style to errors by value (* Complete Codebase refactor)
    @PostMapping("/{id}/profile-picture")
    public ResponseEntity<?> setProfilePicture(@PathVariable Long id, @RequestBody Map<String, Long> request) throws Exception {
        // TODO: check whether attachment exists -> check if user has permission for it
        Long attachmentId = request.get("attachmentId");
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new BadRequestException("Invalid Attachment Id it seems ..."));

        if (userService.setProfilePicture(id, attachment)) {
            return ResponseEntity.ok(Map.of("updated_attachment_id", attachmentId));
        }
        return ResponseEntity.badRequest().body("Failed to update attachment_id");
    }

    @PostMapping("/{id}/description")
    public ResponseEntity<?> updateDescription(@PathVariable Long id, @RequestBody Map<String, String> request) throws Exception {
        String description = request.get("description");
        if (description == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "description is required"));
        }

        if (userService.updateDescription(id, description)) {
            return ResponseEntity.ok(Map.of("message", "Description updated successfully"));
        }
        return ResponseEntity.badRequest().body("Failed to update description");
    }
}
