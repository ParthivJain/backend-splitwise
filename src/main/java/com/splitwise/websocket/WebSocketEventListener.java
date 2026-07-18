package com.splitwise.websocket;

import com.splitwise.service.OnlineUserService;

import org.hibernate.Session;
import org.hibernate.service.spi.Stoppable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;


@Component
public class WebSocketEventListener {

    @Autowired
    private OnlineUserService onlineUserService;

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(event.getMessage());

        Long userId = (Long) accessor.getSessionAttributes().get("userId");

        if (userId != null) {
            onlineUserService.userOffline(userId);
        }

    }
}
