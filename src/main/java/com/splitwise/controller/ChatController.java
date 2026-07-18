package com.splitwise.controller;

import com.splitwise.model.ChatMessage;
import com.splitwise.model.Friend;
import com.splitwise.model.User;
import com.splitwise.repository.FriendRepository;
import com.splitwise.service.ChatPresenceService;
import com.splitwise.service.ChatService;
import com.splitwise.service.FriendService;
import com.splitwise.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private FriendService friendService;

    @Autowired
    private UserService userService;

    @Autowired
    private ChatPresenceService chatPresenceService;

    @Autowired
    private FriendRepository friendRepository;

    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessage message) {
        Friend friend = friendService.getFriendById(message.getReceiverId());

        User sender = friend.getUser();
        User receiver = friend.getFriend();

        ChatMessage saved = chatService.saveMessage(sender.getId(), receiver.getId(), message.getMessage());

        messagingTemplate.convertAndSend("/topic/messages", saved);
        messagingTemplate.convertAndSend("/topic/friends",saved);

    }

    @GetMapping("/{friendId}")
    public List<ChatMessage> getHistory(@PathVariable Long friendId) {
        Long userId = getCurrentUserId();
        Friend friend = friendService.getFriendById(friendId);
        Long friendUserId = friend.getFriend().getId();
        return chatService.getChatHistory(userId, friendUserId);
    }

    @PostMapping("/open/{friendId}")
    public void openChat(@PathVariable Long friendId){
        Long userId = getCurrentUserId();
        Friend friend = friendService.getFriendById(friendId);
        User f = friend.getFriend();
        Long fId = f.getId();

        chatPresenceService.openChat(userId, fId);

    }

    @PostMapping("/close/{friendId}")
    public void closeChat(@PathVariable Long friendId){
        Long userId = getCurrentUserId();
        Friend friend = friendService.getFriendById(friendId);
        User f = friend.getFriend();
        Long fId = f.getId();

        chatPresenceService.closeChat(userId);


    }

    @DeleteMapping("/{messageId}/me")
    public ChatMessage deleteForMe(@PathVariable Long messageId) {
        Long userId = getCurrentUserId();
        return chatService.deleteForMe(messageId, userId);
    }

    @DeleteMapping("/{messageId}/everyone")
    public ChatMessage deleteForEveryone(@PathVariable Long messageId) {
        ChatMessage update = chatService.deleteForEveryone(messageId);
        messagingTemplate.convertAndSend(
                "/topic/messages",
                update
        );
        return update;
    }

    private Long getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    @PutMapping("/{messageId}")
    public ResponseEntity<ChatMessage> editMessage(@PathVariable Long messageId, @RequestBody String newMessage){
        return ResponseEntity.ok(chatService.editMessage(messageId, newMessage));
    }

}