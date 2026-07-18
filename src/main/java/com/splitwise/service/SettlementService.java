package com.splitwise.service;

import com.splitwise.dto.SettlementRequest;
import com.splitwise.dto.SettlementResponseDTO;
import com.splitwise.dto.SettlementSuggestionDTO;
import com.splitwise.model.Friend;
import com.splitwise.model.User;
import com.splitwise.repository.FriendRepository;
import com.splitwise.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SettlementService {

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public SettlementResponseDTO getSettlementSuggestion(Long currentUserId, List<Long> friendIds){
        if(friendIds == null || friendIds.isEmpty()){
            throw new RuntimeException("Please select at least one friend");
        }

        List<User> allusers = new ArrayList<>();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        allusers.add(currentUser);

        List<User> friends = userRepository.findAllById(friendIds);
        allusers.addAll(friends);

        Map<Long, Double> balances = new HashMap<>();
        allusers.forEach(user -> balances.put(user.getId(), 0.0));

        for(User friend : friends){
            Friend friendRelation = friendRepository.findByUserAndFriend(currentUser, friend).orElse(null);

            if(friendRelation != null){
                Double balance = friendRelation.getBalance();
                if(balance != null){
                    balances.put(currentUserId, balances.get(currentUserId) - balance);
                    balances.put(friend.getId(), balances.get(friend.getId()) + balance);
                }
            }
        }

        List<BalanceUser> positive = new ArrayList<>();
        List<BalanceUser> negative = new ArrayList<>();

        positive.sort((a,b) -> Double.compare(b.amount, a.amount));
        negative.sort((a,b) -> Double.compare(b.amount, a.amount));

        for(Map.Entry<Long, Double> entry : balances.entrySet()){
            Long userId = entry.getKey();
            Double amount = entry.getValue();

            if(amount > 0){
                User user = allusers.stream()
                        .filter(u -> u.getId().equals(userId))
                        .findFirst()
                        .orElse(null);
                if(user != null){
                    positive.add(new BalanceUser(userId, user.getName(), amount));
                }
            } else if (amount < 0){
                User user = allusers.stream()
                        .filter(u -> u.getId().equals(userId))
                        .findFirst()
                        .orElse(null);
                if (user != null) {
                    negative.add(new BalanceUser(userId, user.getName(), Math.abs(amount)));
                }
            }
        }

        List<SettlementSuggestionDTO> suggestions = new ArrayList<>();
        int i=0, j=0;
        while(i < negative.size() && j<positive.size()){
            BalanceUser debtor = negative.get(i);
            BalanceUser creditor = positive.get(j);

            double amount = Math.min(debtor.amount, creditor.amount);

            if(amount  > 0.01) {
                SettlementSuggestionDTO suggestion = new SettlementSuggestionDTO();
                suggestion.setFromUserId(debtor.userId);
                suggestion.setFromUserName(debtor.name);
                suggestion.setToUserId(creditor.userId);
                suggestion.setToUserName(creditor.name);
                suggestion.setAmount(Math.round(amount * 100.0) / 100.0);
                suggestions.add(suggestion);
            }

            debtor.amount -= amount;
            creditor.amount -= amount;

            if(debtor.amount < 0.01) i++;
            if(creditor.amount < 0.01) j++;

        }

        SettlementResponseDTO response = new SettlementResponseDTO();

        response.setSuggestions(suggestions);
        response.setTotalTransactions(suggestions.size());
        response.setMessage(suggestions.isEmpty() ? "All settled! 🎉" : "Settlement suggestions generated");

        return response;

    }

    private static class BalanceUser {
        Long userId;
        String name;
        Double amount;

        BalanceUser(Long userId, String name, Double amount) {
            this.userId = userId;
            this.name = name;
            this.amount = amount;
        }
    }

}
