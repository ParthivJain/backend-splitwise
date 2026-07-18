package com.splitwise.service;

import com.splitwise.model.Friend;
import com.splitwise.model.Transaction;
import com.splitwise.model.User;
import com.splitwise.repository.FriendRepository;
import com.splitwise.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private FriendService friendService;

    @Autowired
    private FriendRepository friendRepository;

    public Transaction addTransaction(User user, Friend friend, Double amount, String reason, String type){
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setFriend(friend);
        transaction.setAmount(amount);
        transaction.setReason(reason);
        transaction.setType(type);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setStatus("pending");
        transaction.setIsDeleted(false);

        updateFriendBalance(friend, amount, type);

        LocalDateTime now = LocalDateTime.now();

        friend.setLastActivity(now);

        friendRepository.save(friend);

        return transactionRepository.save(transaction);
    }

    public List<Transaction> getTransactions(User user){
        return transactionRepository.findByUserOrderByTransactionDateDesc(user);
    }

    public List<Transaction> getTransactionByFriendAndUser(User user, Friend friend){
        return transactionRepository.findByUserAndFriendOrderByTransactionDateAsc(user, friend);
    }

    public Transaction deleteTransaction(Long transactionId){
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction Not Found"));

        transaction.setIsDeleted(true);
        return transactionRepository.save(transaction);
    }

    private void updateFriendBalance(Friend friend, Double amount, String type){
        if("given".equals(type)) friend.setBalance(friend.getBalance() + amount);
        else friend.setBalance(friend.getBalance() - amount);
        friendService.updateFriend(friend);
    }

    public Transaction toggleStatus(Long transactionId){
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction Not Found"));

        if ("pending".equals(transaction.getStatus())) {
            transaction.setStatus("settled");
            updateBalanceOnSettle(transaction);
        } else {
            transaction.setStatus("pending");
            updateBalanceOnUnsettle(transaction);
        }

        return transactionRepository.save(transaction);
    }

    private void updateBalanceOnSettle(Transaction transaction) {
        Friend friend = transaction.getFriend();
        if ("given".equals(transaction.getType())) {
            friend.setBalance(friend.getBalance() - transaction.getAmount());
        } else {
            friend.setBalance(friend.getBalance() + transaction.getAmount());
        }
        friendService.updateFriend(friend);
    }

    private void updateBalanceOnUnsettle(Transaction transaction) {
        Friend friend = transaction.getFriend();
        if ("given".equals(transaction.getType())) {
            friend.setBalance(friend.getBalance() + transaction.getAmount());
        } else {
            friend.setBalance(friend.getBalance() - transaction.getAmount());
        }
        friendService.updateFriend(friend);
    }
    public void settleTransactions(User user, User friend){
        Friend friendRelation = friendService.getFriendRelation(user, friend);
        List<Transaction> transactions = transactionRepository.findByFriendAndStatus(friendRelation, "pending");

        for(Transaction t : transactions){
            t.setStatus("settled");
            transactionRepository.save(t);
        }

        friendRelation.setBalance((double) 0);
        friendRepository.save(friendRelation);

    }

}
