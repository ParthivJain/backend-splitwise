package com.splitwise.controller;

import com.splitwise.model.FriendRequest;
import com.splitwise.model.User;
import com.splitwise.service.FriendRequestService;
import com.splitwise.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friend-requests")
public class FriendRequestController {

    @Autowired
    private FriendRequestService friendRequestService;

    @Autowired
    private UserService userService;

    @PostMapping("/{receiverId}")
    public FriendRequest sendRequest(@PathVariable Long receiverId) {
        User sender = getCurrentUser();
        User receiver = userService.getUserById(receiverId);
        return friendRequestService.sendRequest(sender, receiver);
    }

    @GetMapping("/pending")
    public List<FriendRequest> getPendingRequests() {
        User currentUser = getCurrentUser();
        return friendRequestService.getPendingRequestsBySender(currentUser);
    }

    @PutMapping("/notification/{notificationId}/accept")
    public FriendRequest acceptByNotification(@PathVariable Long notificationId) {
        return friendRequestService.acceptRequestByNotification(notificationId);
    }

    @PutMapping("/notification/{notificationId}/reject")
    public FriendRequest rejectByNotification(@PathVariable Long notificationId) {
        return friendRequestService.rejectRequestByNotification(notificationId);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}