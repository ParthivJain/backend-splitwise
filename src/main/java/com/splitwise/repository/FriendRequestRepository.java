package com.splitwise.repository;

import com.splitwise.model.FriendRequest;
import com.splitwise.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long>{

    List<FriendRequest> findBySenderAndStatus(User sender, String status);

    Optional<FriendRequest> findBySenderAndReceiverAndStatus(User sender, User receiver, String status);

    Optional<FriendRequest> findByNotificationId(Long notificationId);

}
