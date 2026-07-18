package com.splitwise.controller;

import com.splitwise.dto.FriendDTO;
import com.splitwise.model.Friend;
import com.splitwise.model.User;
import com.splitwise.repository.FriendRepository;
import com.splitwise.service.FriendService;
import com.splitwise.service.NotificationService;
import com.splitwise.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/friends")
public class FriendController {

    @Autowired
    private FriendService friendService;

    @Autowired
    private UserService userService;

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public List<FriendDTO> getFriends() {
        User currentUser = getCurrentUser();
        return friendService.getActiveFriends(currentUser)
                .stream()
                .map(FriendDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/{friendId}")
    public FriendDTO getFriendById(@PathVariable Long friendId){
        Friend friend = friendService.getFriendById(friendId);
        return FriendDTO.fromEntity(friend);
    }

    @DeleteMapping("/{friendId}")
    public void deleteFriend(@PathVariable Long friendId){

        Friend friend = friendRepository.findById(friendId).orElseThrow();
        User user = friend.getUser();
        User f = friend.getFriend();

        String title = user.getName() + " Deleted you as friend.";
        String message = "If you want to connect with " + user.getName() + ". Send them friend request.";

        notificationService.sendNotification(f, "FriendDelete", title, message);

         friendService.deleteFriend(friendId);
    }

    @PutMapping("/restore/{friendId}")
    public Friend restoreFriend(@PathVariable Long friendId){
        return friendService.restoreFriend(friendId);
    }

    private User getCurrentUser(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

}