package com.splitwise.service;

import com.splitwise.model.Friend;
import com.splitwise.model.User;
import com.splitwise.repository.FriendRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FriendService {

    @Autowired
    private FriendRepository friendRepository;

    public List<Friend> getActiveFriends(User user){
        return friendRepository.findByUserAndStatusOrderByLastActivityDesc(user, "active");
    }

    public boolean areFriends(User user, User friend){
        return friendRepository.findByUserAndFriend(user, friend).isPresent();
    }

    public Friend addFriend(User user, User friend){
        if (areFriends(user, friend)){
            throw new RuntimeException("Already friends!");
        }
        Friend friendship = new Friend();
        friendship.setUser(user);
        friendship.setFriend(friend);
        friendship.setStatus("active");
        friendship.setBalance(0.0);
        return friendRepository.save(friendship);
    }

    public void deleteFriend(Long friendId){
        Friend friend = friendRepository.findById(friendId)
                .orElseThrow();
        User f1 = friend.getUser();
        User f2 = friend.getFriend();
        Friend f = friendRepository.findByUserAndFriend(f2, f1)
                .orElseThrow();
        Long fId = f.getId();
        friendRepository.deleteById(friendId);
        friendRepository.deleteById(fId);
    }

    public Friend restoreFriend(Long friendId){
        Friend friend = friendRepository.findById(friendId)
                .orElseThrow(() -> new RuntimeException("Friend not found"));
        friend.setStatus("active");
        friend.setDeletedByUserId(null);
        friend.setDeletedAt(null);
        friend.setHardDeleteAt(null);
        return friendRepository.save(friend);
    }

    public Friend getFriendById(Long friendId) {
        return friendRepository.findById(friendId)
                .orElseThrow(() -> new RuntimeException("Friend not found!"));
    }

    public Friend getFriendRelation(User user, User friend) {
        return friendRepository.findByUserAndFriend(user, friend)
                .orElseThrow(() -> new RuntimeException("Friend relation not found"));
    }

    public Friend updateFriend(Friend friend) {
        return friendRepository.save(friend);
    }
}

