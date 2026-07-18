package com.splitwise.controller;

import com.splitwise.dto.TransactionDTO;
import com.splitwise.dto.TransactionRequest;
import com.splitwise.dto.TransactionUpdateDTO;
import com.splitwise.model.Friend;
import com.splitwise.model.Transaction;
import com.splitwise.model.User;
import com.splitwise.repository.FriendRepository;
import com.splitwise.repository.TransactionRepository;
import com.splitwise.service.FriendService;
import com.splitwise.service.NotificationService;
import com.splitwise.service.TransactionService;
import com.splitwise.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private FriendService friendService;

    @Autowired
    private UserService userService;

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private NotificationService notificationService;

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTransaction(@PathVariable Long id, @RequestBody TransactionUpdateDTO request){
        try{
            User user = getCurrentUser();
            Long userId = user.getId();

            Transaction transaction = transactionRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Transaction not found"));

            if(!transaction.getUser().getId().equals(userId)){
                return ResponseEntity.status(403).body("Only sender can edit this transaction");
            }

            Double oldAmount = transaction.getAmount();
            Double newAmount = request.getAmount();

            if(newAmount != null && newAmount > 0){
                transaction.setAmount(request.getAmount());
            }
            if(request.getReason() != null && !request.getReason().trim().isEmpty()){
                transaction.setReason(request.getReason().trim());
            }

            Transaction update = transactionRepository.save(transaction);

            updateFriendBalance(transaction, oldAmount, newAmount);

            return ResponseEntity.ok(update);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/add")
    public TransactionDTO addTransactionCpy(@RequestBody TransactionRequest request){
        User currentUser = getCurrentUser();
        User f = userService.getUserById(request.getFriendId());
        Friend friend = friendService.getFriendRelation(currentUser, f);
        Transaction transaction = transactionService.addTransaction(currentUser, friend, request.getAmount(), request.getReason(), request.getType());
        return TransactionDTO.fromEntity(transaction);
    }

    @PostMapping
    public TransactionDTO addTransaction(@RequestBody TransactionRequest request){
        User currentUser = getCurrentUser();
        User f = userService.getUserById(request.getFriendId());
        Friend friend = friendService.getFriendRelation(currentUser, f);
        Transaction transaction = transactionService.addTransaction(currentUser, friend, request.getAmount(), request.getReason(), request.getType());

        String title = String.format("%s has added a new transaction with you.", currentUser.getName());
        String message = "friendId : "+ currentUser.getId() +
                "\namount: ₹" + request.getAmount() +
                "\nreason: " + request.getReason() +
                "\ndate: " + LocalDateTime.now().toLocalDate() +
                "\ntype: " + request.getType();

        notificationService.sendNotification(f, "transaction_add", title, message);

        return TransactionDTO.fromEntity(transaction);
    }

    @PostMapping("/settle/notif/{friendId}")
    public String settleNotifTransaction(@PathVariable Long friendId){
        User currentUser = getCurrentUser();
        User friend = userService.getUserById(friendId);
        transactionService.settleTransactions(currentUser, friend);
        return "Transaction settled with notification!";
    }

    @PostMapping("/settle/{friendId}")
    public String settleTransaction(@PathVariable Long friendId){
        User currentUser = getCurrentUser();
        User friend = userService.getUserById(friendId);

        transactionService.settleTransactions(currentUser, friend);

        String title = currentUser.getName() + " has settled all transactions with you";
        String message = "Do you want to settle all transactions with " + currentUser.getName() + "? [friendId:" + currentUser.getId() + "]";

        notificationService.sendNotification(friend, "settlement", title, message);

        return "Transaction settled!";
    }

    @GetMapping
    public List<TransactionDTO> getTransactions() {
        User currentUser = getCurrentUser();
        return transactionService.getTransactions(currentUser)
                .stream()
                .map(TransactionDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/friend/{friendId}")
    public List<TransactionDTO> getTransactionsByFriend(@PathVariable Long friendId){
        User currentUser = getCurrentUser();
        User f = userService.getUserById(friendId);
        Friend friend = friendService.getFriendRelation(currentUser, f);
        return transactionService.getTransactionByFriendAndUser(currentUser, friend)
                .stream()
                .map(TransactionDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @PutMapping("/{transactionId}/toggle")
    public TransactionDTO toggleStatus(@PathVariable Long transactionId){
        Transaction transaction = transactionService.toggleStatus(transactionId);
        return TransactionDTO.fromEntity(transaction);
    }

    @DeleteMapping("/{transactionId}")
    public TransactionDTO deleteTransaction(@PathVariable Long transactionId){
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if(transaction.getStatus().equals("pending")){
            updateFriendBalanceOnDelete(transaction, transaction.getAmount());
        }

        Transaction deleted = transactionService.deleteTransaction(transactionId);
        return TransactionDTO.fromEntity(deleted);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found!"));
    }

    private void updateFriendBalance(Transaction transaction, Double oldAmount, Double newAmount) {
        try {
            User sender = transaction.getUser();
            Friend friend = transaction.getFriend();
            User receiver = friend.getFriend();

            Friend friendRelation = friendRepository
                    .findByUserAndFriend(sender, receiver)
                    .orElseThrow(() -> new RuntimeException("Friend relation not found"));

            Double balanceDiff = newAmount - oldAmount;
            Double currentBalance = friendRelation.getBalance();

            if (transaction.getType().equals("given")) {
                friendRelation.setBalance(currentBalance + balanceDiff);
            } else if (transaction.getType().equals("taken")) {
                friendRelation.setBalance(currentBalance - balanceDiff);
            }

            friendRepository.save(friendRelation);

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private void updateFriendBalanceOnDelete(Transaction transaction, Double amount) {
        try {
            User sender = transaction.getUser();
            Friend friend = transaction.getFriend();
            User receiver = friend.getFriend();

            Friend friendRelation = friendRepository
                    .findByUserAndFriend(sender, receiver)
                    .orElseThrow(() -> new RuntimeException("Friend relation not found"));

            Double currentBalance = friendRelation.getBalance();

            if (transaction.getType().equals("given")) {
                friendRelation.setBalance(currentBalance - amount);
            } else if (transaction.getType().equals("taken")) {
                friendRelation.setBalance(currentBalance + amount);
            }

            friendRepository.save(friendRelation);

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}