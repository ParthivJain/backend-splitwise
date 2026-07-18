package com.splitwise.dto;

import com.splitwise.model.Friend;
import lombok.Data;

@Data
public class FriendDTO {
    private Long id;
    private Long userId;
    private Long friendId;
    private String friendName;
    private String friendUsername;
    private String profilePic;
    private Double balance;
    private String status;
    private boolean hasUnreadMessage;

    public static FriendDTO fromEntity(Friend friend) {
        FriendDTO dto = new FriendDTO();
        dto.setId(friend.getId());
        dto.setUserId(friend.getUser().getId());
        dto.setFriendId(friend.getFriend().getId());
        dto.setFriendName(friend.getFriend().getName());
        dto.setFriendUsername(friend.getFriend().getUsername());
        dto.setProfilePic(friend.getFriend().getProfilePic());
        dto.setBalance(friend.getBalance());
        dto.setStatus(friend.getStatus());
        dto.setHasUnreadMessage(friend.isHasUnreadMessage());
        return dto;
    }
}