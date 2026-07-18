package com.splitwise.service;

import com.splitwise.model.ChatMessage;
import com.splitwise.model.Friend;
import com.splitwise.model.User;
import com.splitwise.repository.ChatRepository;
import com.splitwise.repository.FriendRepository;
import com.splitwise.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatPresenceService chatPresenceService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public ChatMessage saveMessage(Long senderId, Long receiverId, String message) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(senderId);
        chatMessage.setReceiverId(receiverId);
        chatMessage.setMessage(message);
        chatMessage.setCreatedAt(LocalDateTime.now());
        if(chatPresenceService.isViewingChat(receiverId, senderId)){
            chatMessage.setStatus("read");
        } else {chatMessage.setStatus("sent");}

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        Friend senderFriend = friendRepository.findByUserAndFriend(sender, receiver)
                .orElseThrow(() -> new RuntimeException("Friend not found!"));

        Friend receiverFriend = friendRepository.findByUserAndFriend(receiver, sender)
                .orElseThrow(() -> new RuntimeException("Friend not found!"));

        LocalDateTime now = LocalDateTime.now();

        senderFriend.setLastActivity(now);
        receiverFriend.setLastActivity(now);


        receiverFriend.setHasUnreadMessage(true);

        friendRepository.save(receiverFriend);

        friendRepository.save(senderFriend);
        friendRepository.save(receiverFriend);

        return chatRepository.save(chatMessage);
    }

    public List<ChatMessage> getChatHistory(Long userId1, Long userId2){
        return chatRepository.findBySenderIdAndReceiverIdOrReceiverIdAndSenderIdOrderByCreatedAtAsc(userId1, userId2, userId1, userId2);
    }

    public ChatMessage deleteForMe(Long messageId, Long userId){
        ChatMessage message = chatRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message Not Found"));

        if(message.getSenderId().equals(userId)){
            message.setDeletedBySender(true);
        }
        else if(message.getReceiverId().equals(userId)) message.setDeletedByReceiver(true);
        return chatRepository.save(message);

    }

    public ChatMessage deleteForEveryone(Long messageId){
        ChatMessage message = chatRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message Not Found"));
        message.setDeletedForEveryone(true);
        return chatRepository.save(message);
    }

    public ChatMessage editMessage(Long messageId, String newMessage){

        ChatMessage chatMessage = chatRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        chatMessage.setMessage(newMessage);

        ChatMessage updated = chatRepository.save(chatMessage);

        messagingTemplate.convertAndSend("/topic/messages", updated);

        return updated;

    }

}
