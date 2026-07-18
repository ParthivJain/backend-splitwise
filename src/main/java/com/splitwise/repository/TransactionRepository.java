package com.splitwise.repository;

import com.splitwise.model.Friend;
import com.splitwise.model.Transaction;
import com.splitwise.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserAndIsDeletedFalseOrderByTransactionDateDesc(User user);

    List<Transaction> findByUserAndFriendAndIsDeletedFalseOrderByTransactionDateDesc(User user, Friend friend);

    List<Transaction> findByUserAndFriendAndStatusAndIsDeletedFalseOrderByTransactionDateDesc(User user, Friend friend, String status);

    List<Transaction> findByUserAndFriendOrderByTransactionDateAsc(User user, Friend friend);

    List<Transaction> findByFriendAndStatus(Friend friend, String status);

    List<Transaction> findByUserOrderByTransactionDateDesc(User user);
}