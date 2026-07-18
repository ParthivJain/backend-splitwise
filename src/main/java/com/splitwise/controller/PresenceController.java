package com.splitwise.controller;

import com.splitwise.dto.UserStatusDTO;
import com.splitwise.model.User;
import com.splitwise.service.OnlineUserService;
import com.splitwise.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/presence")
public class PresenceController {

    @Autowired
    private OnlineUserService onlineUserService;

    @Autowired
    private UserService userService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/online")
    public Set<Long> getOnlineUsers() {
        return onlineUserService.getOnlineUsers();
    }

    @PostMapping("/online")
    public void online(){
        Long userId = getCurrentUserId();
        onlineUserService.userOnline(userId);
        messagingTemplate.convertAndSend(
                "/topic/status",
                new UserStatusDTO(userId, true)
        );
    }

    @PostMapping("/offline")
    public void offline(){
        Long userId = getCurrentUserId();
        onlineUserService.userOffline(userId);
        messagingTemplate.convertAndSend(
                "/topic/status",
                new UserStatusDTO(userId, false)
        );
    }

    private Long getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

}
