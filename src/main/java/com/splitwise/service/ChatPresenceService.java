package com.splitwise.service;

import com.splitwise.model.User;
import com.splitwise.model.Friend;
import com.splitwise.repository.ChatRepository;
import com.splitwise.repository.FriendRepository;
import com.splitwise.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatPresenceService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private ChatRepository chatRepository;

    private final ConcurrentHashMap<Long, Long> activeChats = new ConcurrentHashMap<>();

    public void openChat(Long userId, Long friendId) {
        activeChats.put(userId, friendId);
        chatRepository.markMessagesAsRead(userId, friendId);

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        User friend = userRepository.findById(friendId).orElseThrow(() -> new RuntimeException("User not found"));

        Friend friendRecord = friendRepository.findByUserAndFriend(user, friend).orElseThrow();

        friendRecord.setHasUnreadMessage(false);

        friendRepository.save(friendRecord);

    }

    public void closeChat(Long userId) {
        activeChats.remove(userId);
    }

    public boolean isViewingChat(Long userId, Long friendId) {
        return (activeChats.containsKey(userId) && activeChats.get(userId).equals(friendId));
    }
}