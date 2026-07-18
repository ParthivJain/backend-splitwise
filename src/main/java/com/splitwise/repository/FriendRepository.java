package com.splitwise.repository;

import com.splitwise.model.Friend;
import com.splitwise.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {

    Optional<Friend> findById(Long id);

    List<Friend> findByUserAndStatusOrderByLastActivityDesc(User user, String status);

    Optional<Friend> findByUserAndFriend(User user, User friend);

    List<Friend> findByUserAndStatusAndHardDeleteAtBefore(User user, String status, LocalDateTime date);
}