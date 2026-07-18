package com.splitwise.service;

import com.splitwise.model.FriendRequest;
import com.splitwise.model.Notification;
import com.splitwise.model.User;
import com.splitwise.repository.FriendRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FriendRequestService {

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private FriendService friendService;

    @Autowired
    private NotificationService notificationService;

    public FriendRequest sendRequest(User sender, User receiver) {
        if (friendService.areFriends(sender, receiver)) {
            throw new RuntimeException("Already friends!");
        }

        if (friendRequestRepository.findBySenderAndReceiverAndStatus(sender, receiver, "pending").isPresent()) {
            throw new RuntimeException("Request already sent!");
        }

        FriendRequest request = new FriendRequest();
        request.setSender(sender);
        request.setReceiver(receiver);
        request.setStatus("pending");
        FriendRequest savedRequest = friendRequestRepository.save(request);

        Notification notification = notificationService.sendNotification(
                receiver,
                "friend_request",
                sender.getName() + " sent you a friend request",
                "Accept or reject the request"
        );

        savedRequest.setNotificationId(notification.getId());
        return friendRequestRepository.save(savedRequest);
    }

    public FriendRequest acceptRequestByNotification(Long notificationId) {
        FriendRequest request = friendRequestRepository.findByNotificationId(notificationId)
                .orElseThrow(() -> new RuntimeException("Request not found for notification: " + notificationId));

        request.setStatus("accepted");
        friendService.addFriend(request.getSender(), request.getReceiver());
        friendService.addFriend(request.getReceiver(), request.getSender());

        notificationService.deleteNotification(notificationId);

        notificationService.sendNotification(
                request.getSender(),
                "friend_accepted",
                request.getReceiver().getName() + " accepted your friend request",
                "You are now friends!"
        );

        return friendRequestRepository.save(request);
    }

    public FriendRequest rejectRequestByNotification(Long notificationId) {
        FriendRequest request = friendRequestRepository.findByNotificationId(notificationId)
                .orElseThrow(() -> new RuntimeException("Request not found for notification: " + notificationId));

        request.setStatus("rejected");

        notificationService.deleteNotification(notificationId);

        return friendRequestRepository.save(request);
    }

    public List<FriendRequest> getPendingRequests(User user) {
        return friendRequestRepository.findBySenderAndStatus(user, "pending");
    }

    public List<FriendRequest> getPendingRequestsBySender(User sender) {
        return friendRequestRepository.findBySenderAndStatus(sender, "pending");
    }
}