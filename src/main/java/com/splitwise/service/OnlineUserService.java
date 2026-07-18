package com.splitwise.service;

import com.splitwise.dto.UserStatusDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OnlineUserService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final Set<Long> onlineUsers = ConcurrentHashMap.newKeySet();

    public void userOnline(Long userId){
        onlineUsers.add(userId);
    }

    public void userOffline(Long userId){
        onlineUsers.remove(userId);
        messagingTemplate.convertAndSend(
                "/topic/status",
                new UserStatusDTO(userId, false)
        );
    }

    public boolean isOnline(Long userId) {
        return onlineUsers.contains(userId);
    }

    public Set<Long> getOnlineUsers() {
        return onlineUsers;
    }

}
